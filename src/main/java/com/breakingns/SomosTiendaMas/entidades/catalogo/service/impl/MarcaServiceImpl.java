package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.MarcaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Vendedor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VendedorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMarcaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarcaServiceImpl implements IMarcaService {

    private final MarcaRepository repo;
    private final VendedorRepository vendedorRepo;

    // ...existing code...
    public MarcaServiceImpl(MarcaRepository repo, VendedorRepository vendedorRepo) {
        this.repo = repo;
        this.vendedorRepo = vendedorRepo;
    }

    @Override
    public MarcaResponseDTO crear(MarcaCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getSlug() != null && repo.findBySlugAndDeletedAtIsNull(dto.getSlug()).isPresent()) {
            throw new IllegalStateException("Slug de marca ya existe");
        }

        Marca m = new Marca();
        m.setNombre(dto.getNombre());
        m.setSlug(dto.getSlug());
        m.setDescripcion(dto.getDescripcion());
        if (dto.getCreadaPorUsuario() != null) m.setCreadaPorUsuario(dto.getCreadaPorUsuario());

        // Si viene creadaPorVendedorId, buscar y setear la entidad Vendedor
        if (dto.getCreadaPorVendedorId() != null) {
            Vendedor v = vendedorRepo.findById(dto.getCreadaPorVendedorId())
                    .orElseThrow(() -> new EntityNotFoundException("Vendedor no encontrado: " + dto.getCreadaPorVendedorId()));
            m.setCreadaPor(v);
        }

        Marca saved = repo.save(m);
        return MarcaMapper.toResponse(saved);
    }

    @Override
    public MarcaResponseDTO actualizar(Long id, MarcaActualizarDTO dto) {
        Marca m = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Marca no encontrada: " + id));
        if (dto.getNombre() != null) m.setNombre(dto.getNombre());
        if (dto.getSlug() != null) {
            repo.findBySlugAndDeletedAtIsNull(dto.getSlug())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new IllegalStateException("Slug ya en uso"); });
            m.setSlug(dto.getSlug());
        }
        if (dto.getDescripcion() != null) m.setDescripcion(dto.getDescripcion());
        Marca updated = repo.save(m);
        return MarcaMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaResponseDTO obtenerPorId(Long id) {
        Marca m = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Marca no encontrada: " + id));
        if (m.getDeletedAt() != null) throw new EntityNotFoundException("Marca eliminada: " + id);
        return MarcaMapper.toResponse(m);
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaResponseDTO obtenerPorSlug(String slug) {
        Marca m = repo.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new EntityNotFoundException("Marca no encontrada por slug: " + slug));
        return MarcaMapper.toResponse(m);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaResumenDTO> listarActivas() {
        return repo.findAll().stream()
                .filter(m -> m.getDeletedAt() == null)
                .map(m -> {
                    MarcaResumenDTO r = new MarcaResumenDTO();
                    r.setId(m.getId());
                    r.setNombre(m.getNombre());
                    r.setSlug(m.getSlug());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaResumenDTO> listarPorCategoria(Long categoriaId) {
        List<Marca> marcas = repo.findDistinctByCategorias_IdAndDeletedAtIsNullOrderByNombreAsc(categoriaId);
        return marcas.stream().map(m -> {
            MarcaResumenDTO r = new MarcaResumenDTO();
            r.setId(m.getId());
            r.setNombre(m.getNombre());
            r.setSlug(m.getSlug());
            return r;
        }).collect(Collectors.toList());
    }

    @Override
    public void eliminarLogico(Long id, String usuario) {
        Marca m = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Marca no encontrada: " + id));
        m.setDeletedAt(LocalDateTime.now());
        if (usuario != null) m.setUpdatedBy(usuario);
        repo.save(m);
    }
}