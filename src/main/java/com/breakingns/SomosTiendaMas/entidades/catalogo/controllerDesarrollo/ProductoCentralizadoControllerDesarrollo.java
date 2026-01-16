package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoDetalleResponseDTO;
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
    public ResponseEntity<ProductoDetalleResponseDTO> crear(@Valid @RequestBody ProductCreateDTO dto) {
        ProductoDetalleResponseDTO created = service.crear(dto);
        URI loc = URI.create("/dev/api/productos/" + (created != null ? created.getProducto().getId() : ""));
        return ResponseEntity.created(loc).body(created);
    }
}
