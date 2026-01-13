/* package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioVarianteController {

    private final IInventarioVarianteService service;

    public InventarioVarianteController(IInventarioVarianteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InventarioVarianteDTO> crear(@RequestBody @Valid InventarioVarianteDTO dto) {
        InventarioVarianteDTO creado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<InventarioVarianteDTO> obtenerPorProductoId(@PathVariable Long productoId) {
        InventarioVarianteDTO dto = service.obtenerPorProductoId(productoId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/producto/{productoId}/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> disponibilidad(@PathVariable Long productoId) {
        DisponibilidadResponseDTO resp = service.disponibilidad(productoId);
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

    @PostMapping("/producto/{productoId}/ajustar")
    public ResponseEntity<InventarioVarianteDTO> ajustarStock(
            @PathVariable Long productoId,
            @RequestParam(required = false, defaultValue = "0") long deltaOnHand,
            @RequestParam(required = false, defaultValue = "0") long deltaReserved) {
        InventarioVarianteDTO dto = service.ajustarStock(productoId, deltaOnHand, deltaReserved);
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

    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<Void> eliminarPorProductoId(@PathVariable Long productoId) {
        service.eliminarPorProductoId(productoId);
        return ResponseEntity.noContent().build();
    }
}
*/