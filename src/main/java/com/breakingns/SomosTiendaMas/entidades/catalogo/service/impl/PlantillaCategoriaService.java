package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.PlantillaCategoriaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PlantillaCategoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.PlantillaCategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPlantillaCategoriaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlantillaCategoriaService implements IPlantillaCategoriaService {

    private final PlantillaCategoriaRepository repo;
    private final CategoriaRepository categoriaRepo;

    public PlantillaCategoriaService(PlantillaCategoriaRepository repo, CategoriaRepository categoriaRepo) {
        this.repo = repo;
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public PlantillaCategoriaResponseDTO crear(PlantillaCategoriaCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getCategoriaId() == null) throw new IllegalArgumentException("categoriaId es requerido");

        // validar existencia de categoria
        Categoria c = categoriaRepo.findById(dto.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada: " + dto.getCategoriaId()));

        // nombre único por categoria (insensible a mayúsculas)
        repo.findByCategoriaIdAndNombreIgnoreCase(dto.getCategoriaId(), dto.getNombre())
                .ifPresent(p -> { throw new IllegalStateException("Ya existe una plantilla con ese nombre en la categoría"); });

        PlantillaCategoria entidad = PlantillaCategoriaMapper.fromCrearDTO(dto);
        entidad.setCategoria(c);

        PlantillaCategoria saved = repo.save(entidad);
        return PlantillaCategoriaMapper.toResponseDTO(saved);
    }

    @Override
    public PlantillaCategoriaResponseDTO actualizar(Long id, PlantillaCategoriaActualizarDTO dto) {
        PlantillaCategoria existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada: " + id));

        // si se cambia el nombre validar unicidad dentro de la misma categoría
        if (dto.getNombre() != null) {
            repo.findByCategoriaIdAndNombreIgnoreCase(existing.getCategoria().getId(), dto.getNombre())
                    .filter(p -> !p.getId().equals(id))
                    .ifPresent(p -> { throw new IllegalStateException("Nombre ya en uso en la categoría"); });
        }

        PlantillaCategoriaMapper.updateFromActualizarDTO(existing, dto);
        PlantillaCategoria updated = repo.save(existing);
        return PlantillaCategoriaMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaCategoriaResponseDTO obtenerPorId(Long id) {
        PlantillaCategoria p = repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada: " + id));
        return PlantillaCategoriaMapper.toResponseDTO(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaCategoriaResumenDTO> listarPorCategoriaId(Long categoriaId) {
        List<PlantillaCategoria> list = repo.findByCategoriaId(categoriaId);
        return list.stream().map(PlantillaCategoriaMapper::toResumenDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaCategoriaResponseDTO> listarActivas() {
        List<PlantillaCategoria> list = repo.findAllByDeletedAtIsNullOrderByNombreAsc();
        return list.stream().map(PlantillaCategoriaMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        PlantillaCategoria p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada: " + id));
        p.setDeletedAt(LocalDateTime.now());
        repo.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaCategoriaResponseDTO obtenerPorCategoriaIdYNombre(Long categoriaId, String nombre) {
        return repo.findByCategoriaIdAndNombreIgnoreCase(categoriaId, nombre)
                .map(PlantillaCategoriaMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada con ese nombre en la categoría"));
    }
}