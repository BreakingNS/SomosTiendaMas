package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev/api")
public class VarianteFisicoControllerDesarrollo {

    private final IVarianteFisicoService service;

    public VarianteFisicoControllerDesarrollo(IVarianteFisicoService service) {
        this.service = service;
    }

    @GetMapping("/variantes/{varianteId}/physical")
    public ResponseEntity<PhysicalPropertiesDTO> obtenerPorVariante(@PathVariable Long varianteId) {
        PhysicalPropertiesDTO dto = service.obtenerPorVarianteId(varianteId);
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/variantes/{varianteId}/physical")
    public ResponseEntity<PhysicalPropertiesDTO> crearOActualizar(
            @PathVariable Long varianteId,
            @Valid @RequestBody PhysicalPropertiesDTO dto) {

        PhysicalPropertiesDTO saved = service.crearOActualizarPorVariante(varianteId, dto);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/variantes/{varianteId}/physical")
    public ResponseEntity<Void> eliminar(@PathVariable Long varianteId) {
        service.eliminarPorVarianteId(varianteId);
        return ResponseEntity.noContent().build();
    }
}