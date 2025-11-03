package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionValorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpcionValorService implements IOpcionValorService {

    private final OpcionValorRepository repo;
    private final OpcionRepository opcionRepo;

    public OpcionValorService(OpcionValorRepository repo, OpcionRepository opcionRepo) {
        this.repo = repo;
        this.opcionRepo = opcionRepo;
    }

    @Override
    public OpcionValorResponseDTO crear(OpcionValorCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getOpcionId() == null) throw new IllegalArgumentException("opcionId es requerido");

        // evitar duplicados por valor en la misma opción
        repo.findByOpcionIdAndValorIgnoreCase(dto.getOpcionId(), dto.getValor())
                .ifPresent(v -> { throw new IllegalStateException("Valor ya existe para la opción"); });

        Opcion opcion = opcionRepo.findById(dto.getOpcionId())
                .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + dto.getOpcionId()));

        OpcionValor entidad = OpcionValorMapper.fromCrear(dto);
        entidad.setOpcion(opcion);

        // calcular orden si no viene
        if (entidad.getOrden() == null) {
            List<OpcionValor> actuales = repo.findByOpcionIdOrderByOrdenAsc(opcion.getId());
            int next = actuales.isEmpty() ? 0 : (actuales.get(actuales.size() - 1).getOrden() + 1);
            entidad.setOrden(next);
        }

        OpcionValor saved = repo.save(entidad);
        return OpcionValorMapper.toResponse(saved);
    }

    @Override
    public OpcionValorResponseDTO actualizar(Long id, OpcionValorActualizarDTO dto) {
        OpcionValor existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + id));

        // si cambia valor, validar duplicado
        if (dto.getValor() != null) {
            repo.findByOpcionIdAndValorIgnoreCase(existing.getOpcion().getId(), dto.getValor())
                    .filter(v -> !v.getId().equals(id))
                    .ifPresent(v -> { throw new IllegalStateException("Valor ya existe para la opción"); });
        }

        OpcionValorMapper.applyActualizar(dto, existing);
        OpcionValor updated = repo.save(existing);
        return OpcionValorMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionValorResponseDTO obtenerPorId(Long id) {
        OpcionValor v = repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + id));
        return OpcionValorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionValorResponseDTO obtenerPorSlug(String slug) {
        return repo.findBySlug(slug)
                .map(OpcionValorMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado por slug: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionValorResponseDTO> listarPorOpcionId(Long opcionId) {
        List<OpcionValor> list = repo.findByOpcionIdOrderByOrdenAsc(opcionId);
        return list.stream()
                .filter(v -> v.getDeletedAt() == null)
                .map(OpcionValorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionValorResponseDTO> listarActivos() {
        return repo.findAllByDeletedAtIsNullOrderByOrdenAsc().stream()
                .map(OpcionValorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeValorEnOpcion(Long opcionId, String valor) {
        return repo.findByOpcionIdAndValorIgnoreCase(opcionId, valor).isPresent();
    }

    @Override
    public void eliminar(Long id) {
        OpcionValor v = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + id));
        v.setDeletedAt(LocalDateTime.now());
        repo.save(v);
    }

    @Override
    public void eliminarPorOpcionId(Long opcionId) {
        // deleteByOpcionId hace borrado físico; si prefieres soft-delete, itera y setea deletedAt
        repo.deleteByOpcionId(opcionId);
    }
}
