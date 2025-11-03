package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.EtiquetaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IEtiquetaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EtiquetaServiceImpl implements IEtiquetaService {

    private final EtiquetaRepository repo;

    public EtiquetaServiceImpl(EtiquetaRepository repo) {
        this.repo = repo;
    }

    @Override
    public EtiquetaResponseDTO crear(EtiquetaCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getSlug() != null && repo.findBySlugAndDeletedAtIsNull(dto.getSlug()).isPresent()) {
            throw new IllegalStateException("Slug de etiqueta ya existe");
        }
        Etiqueta entidad = EtiquetaMapper.fromCrear(dto);
        Etiqueta saved = repo.save(entidad);
        return EtiquetaMapper.toResponse(saved);
    }

    @Override
    public EtiquetaResponseDTO actualizar(Long id, EtiquetaActualizarDTO dto) {
        Etiqueta e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada: " + id));
        // slug único si se modifica
        if (dto.getSlug() != null) {
            repo.findBySlugAndDeletedAtIsNull(dto.getSlug())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new IllegalStateException("Slug ya en uso"); });
        }
        EtiquetaMapper.applyActualizar(dto, e);
        Etiqueta updated = repo.save(e);
        return EtiquetaMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EtiquetaResponseDTO obtenerPorId(Long id) {
        Etiqueta e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada: " + id));
        return EtiquetaMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public EtiquetaResponseDTO obtenerPorSlug(String slug) {
        Etiqueta e = repo.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada por slug: " + slug));
        return EtiquetaMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtiquetaResumenDTO> listarActivas() {
        return repo.findAllByDeletedAtIsNull().stream()
                .map(EtiquetaMapper::toResumen)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        Etiqueta e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }
}