package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VendedorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VendedorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VendedorAuditoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VendedorAuditoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVendedorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VendedorService implements IVendedorService {

    private final VendedorRepository repo;
    private final VendedorAuditoriaRepository auditoriaRepo;

    public VendedorService(VendedorRepository repo, VendedorAuditoriaRepository auditoriaRepo) {
        this.repo = repo;
        this.auditoriaRepo = auditoriaRepo;
    }

    @Override
    public VendedorResponseDTO crear(VendedorCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getUserId() != null && repo.findByUsuarioIdAndDeletedAtIsNull(dto.getUserId()).isPresent()) {
            throw new IllegalStateException("Ya existe un vendedor para ese userId/usuarioId");
        }
        Vendedor entidad = VendedorMapper.fromCrear(dto);

        // Generar slug si no vino desde el cliente (o es nulo)
        if (entidad.getSlug() == null || entidad.getSlug().isBlank()) {
            String base = entidad.getDisplayName() != null ? entidad.getDisplayName() : entidad.getNombreLegal();
            String candidate = generateSlug(base);
            if (candidate == null || candidate.isBlank()) candidate = "vendedor-" + System.currentTimeMillis();
            String unique = candidate;
            int suffix = 0;
            while (repo.existsBySlugAndDeletedAtIsNull(unique)) {
                suffix++;
                unique = candidate + "-" + suffix;
            }
            entidad.setSlug(unique);
        }

        Vendedor saved = repo.save(entidad);

        // Registrar auditoría de creación
        try {
            VendedorAuditoria aud = new VendedorAuditoria();
            aud.setVendedorId(saved.getId());
            aud.setTipoCambio(VendedorAuditoria.TipoCambio.ACTUALIZADO);
            aud.setFieldName("*CREACION*");
            aud.setOldValue(null);
            aud.setNewValue(VendedorMapper.toResponse(saved).toString());
            aud.setChangedBy(null);
            auditoriaRepo.save(aud);
        } catch (Exception ex) {
            // No queremos que la auditoría evite la creación del vendedor; loguear y seguir
            System.err.println("Error registrando auditoría de vendedor: " + ex.getMessage());
        }

        return VendedorMapper.toResponse(saved);
    }

    @Override
    public VendedorResponseDTO actualizar(Long id, VendedorActualizarDTO dto) {
        Vendedor v = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + id));

        // Capturar estado anterior para auditoría
        String oldValue = VendedorMapper.toResponse(v).toString();

        VendedorMapper.applyActualizar(dto, v);
        Vendedor updated = repo.save(v);

        // Registrar auditoría de actualización (no bloqueante)
        try {
            VendedorAuditoria aud = new VendedorAuditoria();
            aud.setVendedorId(updated.getId());
            aud.setTipoCambio(VendedorAuditoria.TipoCambio.ACTUALIZADO);
            aud.setFieldName("*ACTUALIZACION*");
            aud.setOldValue(oldValue);
            aud.setNewValue(VendedorMapper.toResponse(updated).toString());
            aud.setChangedBy(null);
            auditoriaRepo.save(aud);
        } catch (Exception ex) {
            System.err.println("Error registrando auditoría de vendedor (actualizar): " + ex.getMessage());
        }

        return VendedorMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponseDTO obtenerPorId(Long id) {
        Vendedor v = repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + id));
        return VendedorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponseDTO obtenerPorUserId(Long userId) {
        Vendedor v = repo.findByUsuarioIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado para userId: " + userId));
        return VendedorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public VendedorResponseDTO obtenerPorEmpresaId(Long empresaId) {
        Vendedor v = repo.findByEmpresaIdAndDeletedAtIsNull(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado para empresaId: " + empresaId));
        return VendedorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendedorResumenDTO> listarActivos() {
        List<Vendedor> list = repo.findAllByDeletedAtIsNullOrderByDisplayNameAsc();
        return list.stream().map(VendedorMapper::toResumen).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendedorResumenDTO> buscarPorFiltro(VendedorFiltroDTO filtro) {
        if (filtro == null || filtro.getNombreContains() == null) return listarActivos();
        List<Vendedor> list = repo.findByDisplayNameContainingIgnoreCase(filtro.getNombreContains());
        return list.stream()
                .filter(v -> v.getDeletedAt() == null)
                .map(VendedorMapper::toResumen)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        Vendedor v = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + id));
        v.setDeletedAt(LocalDateTime.now());
        repo.save(v);
    }

    @Override
    public VendedorResponseDTO actualizarPorUserId(Long userId, VendedorActualizarDTO dto) {
        Vendedor v = repo.findByUsuarioIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado para userId: " + userId));
        String oldValue = VendedorMapper.toResponse(v).toString();
        VendedorMapper.applyActualizar(dto, v);
        Vendedor updated = repo.save(v);

        try {
            VendedorAuditoria aud = new VendedorAuditoria();
            aud.setVendedorId(updated.getId());
            aud.setTipoCambio(VendedorAuditoria.TipoCambio.ACTUALIZADO);
            aud.setFieldName("*ACTUALIZACION_POR_USER*");
            aud.setOldValue(oldValue);
            aud.setNewValue(VendedorMapper.toResponse(updated).toString());
            aud.setChangedBy(null);
            auditoriaRepo.save(aud);
        } catch (Exception ex) {
            System.err.println("Error registrando auditoría de vendedor (actualizar por user): " + ex.getMessage());
        }

        return VendedorMapper.toResponse(updated);
    }

    @Override
    public VendedorResponseDTO actualizarPorEmpresaId(Long empresaId, VendedorActualizarDTO dto) {
        Vendedor v = repo.findByEmpresaIdAndDeletedAtIsNull(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado para empresaId: " + empresaId));
        String oldValue = VendedorMapper.toResponse(v).toString();
        VendedorMapper.applyActualizar(dto, v);
        Vendedor updated = repo.save(v);

        try {
            VendedorAuditoria aud = new VendedorAuditoria();
            aud.setVendedorId(updated.getId());
            aud.setTipoCambio(VendedorAuditoria.TipoCambio.ACTUALIZADO);
            aud.setFieldName("*ACTUALIZACION_POR_EMPRESA*");
            aud.setOldValue(oldValue);
            aud.setNewValue(VendedorMapper.toResponse(updated).toString());
            aud.setChangedBy(null);
            auditoriaRepo.save(aud);
        } catch (Exception ex) {
            System.err.println("Error registrando auditoría de vendedor (actualizar por empresa): " + ex.getMessage());
        }

        return VendedorMapper.toResponse(updated);
    }

    @Override
    public void eliminarPorEmpresaId(Long empresaId) {
        if (empresaId == null) return;
        repo.findByEmpresaIdAndDeletedAtIsNull(empresaId).ifPresent(v -> {
            v.setDeletedAt(LocalDateTime.now());
            repo.save(v);
            try {
                VendedorAuditoria aud = new VendedorAuditoria();
                aud.setVendedorId(v.getId());
                aud.setTipoCambio(VendedorAuditoria.TipoCambio.ESTADO);
                aud.setFieldName("*ELIMINACION_SOFT_EMPRESA*");
                aud.setOldValue(null);
                aud.setNewValue("deleted");
                aud.setChangedBy(null);
                auditoriaRepo.save(aud);
            } catch (Exception ex) {
                System.err.println("Error registrando auditoría de vendedor (soft-delete empresa): " + ex.getMessage());
            }
        });
    }

    @Override
    public void purgarPorEmpresaId(Long empresaId) {
        if (empresaId == null) return;
        repo.findByEmpresaId(empresaId).ifPresent(v -> {
            Long vid = v.getId();
            try {
                VendedorAuditoria aud = new VendedorAuditoria();
                aud.setVendedorId(vid);
                aud.setTipoCambio(VendedorAuditoria.TipoCambio.ESTADO);
                aud.setFieldName("*ELIMINACION_FISICA_EMPRESA*");
                aud.setOldValue(null);
                aud.setNewValue("purged");
                aud.setChangedBy(null);
                auditoriaRepo.save(aud);
            } catch (Exception ex) {
                System.err.println("Error registrando auditoría de vendedor (purge-pre empresa): " + ex.getMessage());
            }
        });

        // delete physical by company - repository doesn't have deleteByEmpresaId, so use custom
        // For now perform a find and delete if present
        repo.findByEmpresaId(empresaId).ifPresent(v -> repo.delete(v));
    }

    @Override
    public void eliminarPorUserId(Long userId) {
        if (userId == null) return;
        // Soft-delete: buscar vendedor por usuario y marcar deletedAt
        repo.findByUsuarioIdAndDeletedAtIsNull(userId).ifPresent(v -> {
            v.setDeletedAt(LocalDateTime.now());
            repo.save(v);

            // auditoría
            try {
                VendedorAuditoria aud = new VendedorAuditoria();
                aud.setVendedorId(v.getId());
                aud.setTipoCambio(VendedorAuditoria.TipoCambio.ESTADO);
                aud.setFieldName("*ELIMINACION_SOFT*");
                aud.setOldValue(null);
                aud.setNewValue("deleted");
                aud.setChangedBy(null);
                auditoriaRepo.save(aud);
            } catch (Exception ex) {
                System.err.println("Error registrando auditoría de vendedor (soft-delete): " + ex.getMessage());
            }
        });
    }

    @Override
    public void purgarPorUserId(Long userId) {
        if (userId == null) return;
        // Obtener el vendedor existente (si lo hay) para registrar auditoría con su id
        repo.findByUsuarioId(userId).ifPresent(v -> {
            Long vid = v.getId();
            try {
                VendedorAuditoria aud = new VendedorAuditoria();
                aud.setVendedorId(vid);
                aud.setTipoCambio(VendedorAuditoria.TipoCambio.ESTADO);
                aud.setFieldName("*ELIMINACION_FISICA*");
                aud.setOldValue(null);
                aud.setNewValue("purged");
                aud.setChangedBy(null);
                auditoriaRepo.save(aud);
            } catch (Exception ex) {
                System.err.println("Error registrando auditoría de vendedor (purge-pre): " + ex.getMessage());
            }
        });

        // Ejecutar borrado físico (puede borrar 0 o 1 filas según existencia)
        repo.deleteByUsuarioId(userId);
    }

    // helper slug generator
    private static String generateSlug(String input) {
        if (input == null) return null;
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return normalized;
    }

}