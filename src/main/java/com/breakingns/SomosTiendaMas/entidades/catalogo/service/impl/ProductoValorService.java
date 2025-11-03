package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoValorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoValorService implements IProductoValorService {

    private final ProductoValorRepository repo;
    private final ProductoRepository productoRepo;
    private final OpcionValorRepository valorRepo;

    public ProductoValorService(ProductoValorRepository repo,
                                      ProductoRepository productoRepo,
                                      OpcionValorRepository valorRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
        this.valorRepo = valorRepo;
    }

    @Override
    public OpcionValorResponseDTO asignarValor(Long productoId, Long valorId) {
        if (productoId == null || valorId == null) throw new IllegalArgumentException("productoId y valorId requeridos");

        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));
        OpcionValor valor = valorRepo.findById(valorId)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + valorId));

        // evitar duplicados activos
        repo.findByProductoIdAndValorId(productoId, valorId).ifPresent(rel -> {
            if (rel.getDeletedAt() == null) throw new IllegalStateException("Valor ya asignado al producto");
        });

        ProductoValor rel = new ProductoValor();
        rel.setProducto(producto);
        rel.setValor(valor);
        ProductoValor saved = repo.save(rel);
        return OpcionValorMapper.toResponse(saved.getValor());
    }

    @Override
    public void quitarValor(Long productoId, Long valorId) {
        ProductoValor rel = repo.findByProductoIdAndValorId(productoId, valorId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));
        rel.setDeletedAt(LocalDateTime.now());
        repo.save(rel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionValorResponseDTO> listarValoresPorProductoId(Long productoId) {
        List<ProductoValor> list = repo.findByProductoId(productoId);
        return list.stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(ProductoValor::getValor)
                .filter(v -> v != null && v.getDeletedAt() == null)
                .map(OpcionValorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarProductoIdsPorValorId(Long valorId) {
        List<ProductoValor> list = repo.findByValorId(valorId);
        return list.stream()
                .filter(r -> r.getDeletedAt() == null)
                .map(ProductoValor::getProducto)
                .filter(p -> p != null && p.getDeletedAt() == null)
                .map(Producto::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRelacion(Long productoId, Long valorId) {
        return repo.findByProductoIdAndValorId(productoId, valorId).isPresent();
    }

    @Override
    public void eliminarRelacion(Long id) {
        ProductoValor rel = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Relación no encontrada: " + id));
        rel.setDeletedAt(LocalDateTime.now());
        repo.save(rel);
    }
}