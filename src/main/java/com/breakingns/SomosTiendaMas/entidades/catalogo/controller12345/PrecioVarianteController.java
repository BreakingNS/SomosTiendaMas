/*
package com.breakingns.SomosTiendaMas.entidades.catalogo.controller12345;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.PrecioVarianteMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogo/precios")
public class PrecioVarianteController {

    private final IPrecioVarianteService precioService;

    public PrecioVarianteController(IPrecioVarianteService precioService) {
        this.precioService = precioService;
    }

    @PostMapping
    public ResponseEntity<PrecioVarianteResponseDTO> setPrecio(@RequestBody @Valid PrecioVarianteCrearDTO dto) {
        PrecioVariante p = precioService.setPrecioLista(dto.getVarianteId(), dto.getMontoCentavos());
        return ResponseEntity.ok(PrecioVarianteMapper.toResponse(p));
    }

    @GetMapping("/variantes/{varianteId}/vigente")
    public ResponseEntity<PrecioVarianteResponseDTO> getVigente(@PathVariable Long varianteId) {
        PrecioVariante p = precioService.obtenerPrecioVigente(varianteId).orElseThrow();
        return ResponseEntity.ok(PrecioVarianteMapper.toResponse(p));
    }
}
 */