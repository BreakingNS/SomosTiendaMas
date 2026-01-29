package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoCentralizadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/dev/api/productos/centralizado")
public class ProductoCentralizadoControllerDesarrollo {

    private final IProductoCentralizadoService service;

    public ProductoCentralizadoControllerDesarrollo(IProductoCentralizadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductoCentralizadoResponseFullDTO> crear(@Valid @RequestBody ProductoCentralizadoCrearDTO dto) {
        ProductoCentralizadoResponseFullDTO created = service.crear(dto);
        URI loc = URI.create("/dev/api/productos/centralizado/" + (created != null && created.getProducto() != null ? created.getProducto().getId() : ""));
        return ResponseEntity.created(loc).body(created);
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<ProductoCentralizadoResponseFullDTO> getDetalle(@PathVariable("id") Long id) {
        if (id == null) return ResponseEntity.badRequest().build();
        ProductoCentralizadoResponseFullDTO detalle = service.obtenerPorId(id);
        if (detalle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detalle);
    }

    @GetMapping("/{id}/detalle-simple")
    public ResponseEntity<com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullSimpleDTO> getDetalleSimple(@PathVariable("id") Long id) {
        if (id == null) return ResponseEntity.badRequest().build();
        var detalle = service.obtenerSimplePorId(id);
        if (detalle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(detalle);
    }

}
