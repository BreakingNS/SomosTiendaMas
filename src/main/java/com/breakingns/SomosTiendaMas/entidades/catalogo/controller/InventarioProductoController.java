package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioProductoController {

    private final IInventarioProductoService service;

    public InventarioProductoController(IInventarioProductoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InventarioProductoDTO> crear(@RequestBody @Valid InventarioProductoDTO dto) {
        InventarioProductoDTO creado = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<InventarioProductoDTO> obtenerPorProductoId(@PathVariable Long productoId) {
        InventarioProductoDTO dto = service.obtenerPorProductoId(productoId);
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
    public ResponseEntity<InventarioProductoDTO> ajustarStock(
            @PathVariable Long productoId,
            @RequestParam(required = false, defaultValue = "0") long deltaOnHand,
            @RequestParam(required = false, defaultValue = "0") long deltaReserved) {
        InventarioProductoDTO dto = service.ajustarStock(productoId, deltaOnHand, deltaReserved);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<InventarioProductoDTO>> listarTodos() {
        List<InventarioProductoDTO> list = service.listarTodos();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<InventarioProductoDTO>> listarBajoStock(@RequestParam(required = false) Long threshold) {
        List<InventarioProductoDTO> list = service.listarBajoStock(threshold);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<Void> eliminarPorProductoId(@PathVariable Long productoId) {
        service.eliminarPorProductoId(productoId);
        return ResponseEntity.noContent().build();
    }
}