package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoServiceImpl implements IProductoService {

    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               MarcaRepository marcaRepository,
                               CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.marcaRepository = marcaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizar(Long id, Producto cambios) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        if (cambios.getNombre() != null) p.setNombre(cambios.getNombre());
        if (cambios.getSlug() != null) p.setSlug(cambios.getSlug());
        if (cambios.getDescripcion() != null) p.setDescripcion(cambios.getDescripcion());
        if (cambios.getMarca() != null) p.setMarca(resolveMarca(cambios.getMarca().getId()));
        if (cambios.getCategoria() != null) p.setCategoria(resolveCategoria(cambios.getCategoria().getId()));
        if (cambios.getMetadataJson() != null) p.setMetadataJson(cambios.getMetadataJson());
        return productoRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtener(Long id) {
        return productoRepository.findById(id).filter(p -> p.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorSlug(String slug) {
        return productoRepository.findBySlug(slug).filter(p -> p.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll().stream().filter(p -> p.getDeletedAt() == null).toList();
    }

    @Override
    public void eliminarLogico(Long id, String usuario) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + id));
        p.setDeletedAt(LocalDateTime.now());
        p.setUpdatedBy(usuario);
        productoRepository.save(p);
    }

    private Marca resolveMarca(Long id) {
        if (id == null) return null;
        return marcaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + id));
    }

    private Categoria resolveCategoria(Long id) {
        if (id == null) return null;
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));
    }
}
