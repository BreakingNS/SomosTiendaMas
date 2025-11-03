package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.CategoriaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.ICategoriaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaServiceImpl implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        // verificación slug único (activos)
        if (dto.getSlug() != null && categoriaRepository.findBySlugAndDeletedAtIsNull(dto.getSlug()).isPresent()) {
            throw new IllegalStateException("Slug de categoría ya existe");
        }

        Categoria c = new Categoria();
        c.setNombre(dto.getNombre());
        c.setSlug(dto.getSlug());
        c.setDescripcion(dto.getDescripcion());

        if (dto.getCategoriaPadreId() != null) {
            Categoria padre = categoriaRepository.findById(dto.getCategoriaPadreId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria padre no encontrada"));
            c.setCategoriaPadre(padre);
        }

        Categoria saved = categoriaRepository.save(c);
        return CategoriaMapper.toResponse(saved);
    }

    @Override
    public CategoriaResponseDTO actualizar(Long id, CategoriaActualizarDTO dto) {
        Categoria existing = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada: " + id));

        if (dto.getNombre() != null) existing.setNombre(dto.getNombre());
        if (dto.getSlug() != null) {
            categoriaRepository.findBySlugAndDeletedAtIsNull(dto.getSlug())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> { throw new IllegalStateException("Slug ya en uso"); });
            existing.setSlug(dto.getSlug());
        }
        if (dto instanceof CategoriaActualizarDTO) { /* placeholder if hay más campos */ }

        Categoria updated = categoriaRepository.save(existing);
        return CategoriaMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada: " + id));
        return CategoriaMapper.toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorSlug(String slug) {
        Categoria c = categoriaRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada por slug: " + slug));
        return CategoriaMapper.toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResumenDTO> listarActivas() {
        List<Categoria> list = categoriaRepository.findAllByDeletedAtIsNullOrderByNombreAsc();
        return CategoriaMapper.toResumenList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaArbolDTO> obtenerArbol() {
        // obtener todas y tomar raíces (categoriaPadre == null), mapper construye recursivamente usando hijos
        List<Categoria> all = categoriaRepository.findAllByDeletedAtIsNullOrderByNombreAsc();
        List<Categoria> roots = all.stream()
                .filter(c -> c.getCategoriaPadre() == null)
                .collect(Collectors.toList());
        return CategoriaMapper.toArbolList(roots);
    }

    @Override
    public void eliminar(Long id) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada: " + id));
        c.setDeletedAt(LocalDateTime.now());
        categoriaRepository.save(c);
    }
}