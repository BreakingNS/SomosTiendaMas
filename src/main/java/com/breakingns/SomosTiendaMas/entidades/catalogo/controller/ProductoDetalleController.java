package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.ProductoDetalleResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl.ProductoAggregatorServiceImpl;

@RestController
@RequestMapping("/api/productos")
public class ProductoDetalleController {

    private final ProductoAggregatorServiceImpl aggregator;

    public ProductoDetalleController(ProductoAggregatorServiceImpl aggregator) {
        this.aggregator = aggregator;
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ProductoDetalleResponseDTO> getDetalle(@PathVariable("id") Long id) {
        var detalle = aggregator.buildDetalleById(id);
        if (detalle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detalle);
    }
}