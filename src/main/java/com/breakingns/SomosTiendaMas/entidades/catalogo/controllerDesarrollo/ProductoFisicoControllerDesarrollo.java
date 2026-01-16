package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoFisicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REVIEW: se va a utilizar solo VarianteFisico y no ProductoFisico, por lo que este controlador queda obsoleto
@Deprecated(since="2026-01-15", forRemoval=true)

@RestController
@RequestMapping("/api")
public class ProductoFisicoControllerDesarrollo {

    private final IProductoFisicoService service;

    public ProductoFisicoControllerDesarrollo(IProductoFisicoService service) {
        this.service = service;
    }

    @GetMapping("/productos/{productoId}/physical")
    public ResponseEntity<PhysicalPropertiesDTO> obtenerPorProducto(@PathVariable Long productoId) {
        PhysicalPropertiesDTO dto = service.obtenerPorProductoId(productoId);
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/productos/{productoId}/physical")
    public ResponseEntity<PhysicalPropertiesDTO> crearOActualizar(
            @PathVariable Long productoId,
            @Valid @RequestBody PhysicalPropertiesDTO dto) {

        PhysicalPropertiesDTO saved = service.crearOActualizarPorProducto(productoId, dto);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/productos/{productoId}/physical")
    public ResponseEntity<Void> eliminar(@PathVariable Long productoId) {
        service.eliminarPorProductoId(productoId);
        return ResponseEntity.noContent().build();
    }
}