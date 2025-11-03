package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService implements IProductoService {

    private final ProductoRepository repo;
    private final MarcaRepository marcaRepo;
    private final CategoriaRepository categoriaRepo;

    public ProductoService(ProductoRepository repo, MarcaRepository marcaRepo, CategoriaRepository categoriaRepo) {
        this.repo = repo;
        this.marcaRepo = marcaRepo;
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public ProductoResponseDTO crear(ProductoCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getSlug() != null && repo.findBySlugAndDeletedAtIsNull(dto.getSlug()).isPresent()) {
            throw new IllegalStateException("Slug ya existe");
        }

        Marca marca = null;
        if (dto.getMarcaId() != null) {
            marca = marcaRepo.findById(dto.getMarcaId()).orElseThrow(() -> new EntityNotFoundException("Marca no encontrada: " + dto.getMarcaId()));
        }
        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepo.findById(dto.getCategoriaId()).orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada: " + dto.getCategoriaId()));
        }

        Producto entidad = ProductoMapper.toEntity(dto, marca, categoria);
        Producto saved = repo.save(entidad);
        return ProductoMapper.toResponse(saved);
    }

    @Override
    public ProductoResponseDTO actualizar(Long id, ProductoActualizarDTO dto) {
        Producto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));

        Marca marca = null;
        if (dto.getMarcaId() != null) {
            marca = marcaRepo.findById(dto.getMarcaId()).orElseThrow(() -> new EntityNotFoundException("Marca no encontrada: " + dto.getMarcaId()));
        }
        Categoria categoria = null;
        if (dto.getCategoriaId() != null) {
            categoria = categoriaRepo.findById(dto.getCategoriaId()).orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada: " + dto.getCategoriaId()));
        }

        ProductoMapper.applyActualizar(dto, p, marca, categoria);
        Producto updated = repo.save(p);
        return ProductoMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));
        if (p.getDeletedAt() != null) throw new EntityNotFoundException("Producto eliminado: " + id);
        return ProductoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorSlug(String slug) {
        Producto p = repo.findBySlugAndDeletedAtIsNull(slug).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado por slug: " + slug));
        return ProductoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivas() {
        return repo.findAllByDeletedAtIsNull().stream().map(ProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarPorCategoriaId(Long categoriaId) {
        return repo.findByCategoria_IdAndDeletedAtIsNull(categoriaId).stream().map(ProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarPorMarcaId(Long marcaId) {
        return repo.findByMarca_IdAndDeletedAtIsNull(marcaId).stream().map(ProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        Producto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + id));
        p.setDeletedAt(LocalDateTime.now());
        repo.save(p);
    }
}
