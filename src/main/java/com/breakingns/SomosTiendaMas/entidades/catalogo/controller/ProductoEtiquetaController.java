package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoEtiquetaService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogo/productos/{productoId}/etiquetas")
public class ProductoEtiquetaController {

    private final IProductoEtiquetaService service;

    public ProductoEtiquetaController(IProductoEtiquetaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> asignar(@PathVariable Long productoId, @RequestParam @NotNull Long etiquetaId) {
        service.asignarEtiqueta(productoId, etiquetaId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{etiquetaId}")
    public ResponseEntity<Void> quitar(@PathVariable Long productoId, @PathVariable Long etiquetaId) {
        service.quitarEtiqueta(productoId, etiquetaId);
        return ResponseEntity.noContent().build();
    }
}
