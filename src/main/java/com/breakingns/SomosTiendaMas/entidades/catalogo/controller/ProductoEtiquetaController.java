package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import org.springframework.context.annotation.Profile;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_etiqueta.ProductoEtiquetaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_etiqueta.ProductoEtiquetaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoEtiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoEtiquetaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

// REVIEW: solo en tests, no usar en producción
@RestController
@RequestMapping("/api/producto-etiquetas")
@Profile("test")
public class ProductoEtiquetaController {

    private final ProductoEtiquetaRepository repository;
    private final ProductoRepository productoRepository;
    private final EtiquetaRepository etiquetaRepository;

    public ProductoEtiquetaController(ProductoEtiquetaRepository repository,
                                      ProductoRepository productoRepository,
                                      EtiquetaRepository etiquetaRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
        this.etiquetaRepository = etiquetaRepository;
    }

    @PostMapping
    public ResponseEntity<ProductoEtiquetaResponseDTO> crear(@Valid @RequestBody ProductoEtiquetaCrearDTO dto,
                                                             UriComponentsBuilder uriBuilder) {
        var producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        var etiqueta = etiquetaRepository.findById(dto.getEtiquetaId())
                .orElseThrow(() -> new IllegalArgumentException("Etiqueta no encontrada"));
        ProductoEtiqueta pe = new ProductoEtiqueta();
        pe.setProducto(producto);
        pe.setEtiqueta(etiqueta);
        ProductoEtiqueta saved = repository.save(pe);
        ProductoEtiquetaResponseDTO resp = new ProductoEtiquetaResponseDTO();
        resp.setId(saved.getId());
        resp.setProductoId(producto.getId());
        resp.setEtiquetaId(etiqueta.getId());
        URI location = uriBuilder.path("/api/producto-etiquetas/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoEtiquetaResponseDTO> obtener(@PathVariable Long id) {
        var opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ProductoEtiqueta saved = opt.get();
        ProductoEtiquetaResponseDTO resp = new ProductoEtiquetaResponseDTO();
        resp.setId(saved.getId());
        resp.setProductoId(saved.getProducto().getId());
        resp.setEtiquetaId(saved.getEtiqueta().getId());
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<ProductoEtiquetaResponseDTO>> listar() {
        var list = repository.findAll().stream().map(pe -> {
            ProductoEtiquetaResponseDTO dto = new ProductoEtiquetaResponseDTO();
            dto.setId(pe.getId()); dto.setProductoId(pe.getProducto().getId()); dto.setEtiquetaId(pe.getEtiqueta().getId());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
