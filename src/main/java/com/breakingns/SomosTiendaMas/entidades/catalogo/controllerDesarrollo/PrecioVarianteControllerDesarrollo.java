package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteUIResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/dev/api")
public class PrecioVarianteControllerDesarrollo {

    private final IPrecioVarianteService service;

    public PrecioVarianteControllerDesarrollo(IPrecioVarianteService service) {
        this.service = service;
    }

    // Crear precio para una variante
    @PostMapping("/variantes/{varianteId}/precios")
    public ResponseEntity<PrecioVarianteResponseDTO> crear(@PathVariable Long varianteId, @Valid @RequestBody PrecioVarianteCrearDTO dto,
                                                           UriComponentsBuilder uriBuilder) {
        // asegurar que dto tenga varianteId (compatibilidad)
        dto.setVarianteId(varianteId);
        PrecioVarianteResponseDTO created = service.crear(dto);
        URI location = uriBuilder.path("/dev/api/precios/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Actualizar precio
    @PutMapping("/precios/{id}")
    public ResponseEntity<PrecioVarianteResponseDTO> actualizar(@PathVariable Long id,
                                                                @Valid @RequestBody PrecioVarianteActualizarDTO dto) {
        PrecioVarianteResponseDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Obtener por id
    @GetMapping("/precios/{id}")
    public ResponseEntity<PrecioVarianteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    // Obtener precio vigente para variante (UI)
    @GetMapping("/variantes/{varianteId}/precios/vigente")
    public ResponseEntity<PrecioVarianteUIResumenDTO> obtenerVigenteUI(@PathVariable Long varianteId) {
        PrecioVarianteResponseDTO dto = service.obtenerVigentePorVarianteId(varianteId);
        if (dto == null) return ResponseEntity.noContent().build();

        PrecioVarianteUIResumenDTO ui = new PrecioVarianteUIResumenDTO();
        ui.setProductoId(dto.getProductoId());
        ui.setMontoCentavos(dto.getMontoCentavos());
        ui.setPrecioAnteriorCentavos(dto.getPrecioAnteriorCentavos());
        ui.setPrecioSinIvaCentavos(dto.getPrecioSinIvaCentavos());
        ui.setIvaPorcentaje(dto.getIvaPorcentaje());
        ui.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        ui.setMoneda(dto.getMoneda());
        ui.setActivo(dto.getActivo());
        return ResponseEntity.ok(ui);
    }

    // Listar precios por variante
    @GetMapping("/variantes/{varianteId}/precios")
    public ResponseEntity<List<PrecioVarianteResponseDTO>> listarPorVariante(@PathVariable Long varianteId) {
        return ResponseEntity.ok(service.listarPorVarianteId(varianteId));
    }

    // Listar precios activos (no eliminados)
    @GetMapping("/precios")
    public ResponseEntity<List<PrecioVarianteResponseDTO>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    // Eliminar (soft-delete)
    @DeleteMapping("/precios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Buscar precios vigentes para una fecha opcional (query param fecha ISO) - por variante
    @GetMapping("/variantes/{varianteId}/precios/vigentes")
    public ResponseEntity<List<PrecioVarianteResponseDTO>> buscarVigentesEnFecha(
            @PathVariable Long varianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        List<PrecioVarianteResponseDTO> list = service.buscarVigentesPorVarianteIdEnFecha(varianteId, fecha);
        return ResponseEntity.ok(list);
    }
}