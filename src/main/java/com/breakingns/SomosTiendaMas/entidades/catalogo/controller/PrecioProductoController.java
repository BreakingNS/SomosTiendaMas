/* package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.*;
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
@RequestMapping("/api")
public class PrecioProductoController {

    private final IPrecioVarianteService service;

    public PrecioProductoController(IPrecioVarianteService service) {
        this.service = service;
    }

    // Crear precio
    @PostMapping("/precios")
    public ResponseEntity<PrecioProductoResponseDTO> crear(@Valid @RequestBody PrecioProductoCrearDTO dto,
                                                           UriComponentsBuilder uriBuilder) {
        PrecioProductoResponseDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/precios/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Actualizar precio
    @PutMapping("/precios/{id}")
    public ResponseEntity<PrecioProductoResponseDTO> actualizar(@PathVariable Long id,
                                                                @Valid @RequestBody PrecioProductoActualizarDTO dto) {
        PrecioProductoResponseDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Obtener por id
    @GetMapping("/precios/{id}")
    public ResponseEntity<PrecioProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    // Obtener precio vigente para producto
    @GetMapping("/productos/{productoId}/precios/vigente")
    public ResponseEntity<PrecioProductoUIResumenDTO> obtenerVigenteUI(@PathVariable Long productoId) {
        PrecioProductoResponseDTO dto = service.obtenerVigentePorProductoId(productoId);
        if (dto == null) return ResponseEntity.noContent().build();

        PrecioProductoUIResumenDTO ui = new PrecioProductoUIResumenDTO();
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

    // Listar precios por producto
    @GetMapping("/productos/{productoId}/precios")
    public ResponseEntity<List<PrecioProductoResponseDTO>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(service.listarPorProductoId(productoId));
    }

    // Listar precios activos (no eliminados)
    @GetMapping("/precios")
    public ResponseEntity<List<PrecioProductoResponseDTO>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    // Eliminar (soft-delete)
    @DeleteMapping("/precios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Buscar precios vigentes para una fecha opcional (query param fecha ISO) - método de conveniencia
    @GetMapping("/productos/{productoId}/precios/vigentes")
    public ResponseEntity<List<PrecioProductoResponseDTO>> buscarVigentesEnFecha(
            @PathVariable Long productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        List<PrecioProductoResponseDTO> list = service.buscarVigentesPorProductoIdEnFecha(productoId, fecha);
        return ResponseEntity.ok(list);
    }
}
*/