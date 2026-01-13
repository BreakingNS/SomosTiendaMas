package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioVarianteControllerDesarrollo {

    private final IInventarioVarianteService service;

    public InventarioVarianteControllerDesarrollo(IInventarioVarianteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InventarioVarianteDTO> crear(@RequestBody @Valid InventarioVarianteDTO dto) {
        InventarioVarianteDTO creado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/variante/{varianteId}")
    public ResponseEntity<InventarioVarianteDTO> obtenerPorVarianteId(@PathVariable Long varianteId) {
        InventarioVarianteDTO dto = service.obtenerPorVarianteId(varianteId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/variante/{varianteId}/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> disponibilidad(@PathVariable Long varianteId) {
        DisponibilidadResponseDTO resp = service.disponibilidad(varianteId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reservar")
    public ResponseEntity<ReservaStockResponseDTO> reservarStock(@RequestBody @Valid ReservaStockRequestDTO request) {
        ReservaStockResponseDTO resp = service.reservarStock(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/liberar")
    public ResponseEntity<OperacionSimpleResponseDTO> liberarReserva(@RequestBody @Valid LiberacionReservaRequestDTO request) {
        OperacionSimpleResponseDTO resp = service.liberarReserva(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/confirmar")
    public ResponseEntity<OperacionSimpleResponseDTO> confirmarVenta(@RequestBody @Valid ConfirmacionVentaRequestDTO request) {
        OperacionSimpleResponseDTO resp = service.confirmarVenta(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/variante/{varianteId}/ajustar")
    public ResponseEntity<InventarioVarianteDTO> ajustarStock(
            @PathVariable Long varianteId,
            @RequestParam(required = false, defaultValue = "0") long deltaOnHand,
            @RequestParam(required = false, defaultValue = "0") long deltaReserved) {
        InventarioVarianteDTO dto = service.ajustarStock(varianteId, deltaOnHand, deltaReserved);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<InventarioVarianteDTO>> listarTodos() {
        List<InventarioVarianteDTO> list = service.listarTodos();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<InventarioVarianteDTO>> listarBajoStock(@RequestParam(required = false) Long threshold) {
        List<InventarioVarianteDTO> list = service.listarBajoStock(threshold);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/variante/{varianteId}")
    public ResponseEntity<Void> eliminarPorVarianteId(@PathVariable Long varianteId) {
        service.eliminarPorVarianteId(varianteId);
        return ResponseEntity.noContent().build();
    }
}