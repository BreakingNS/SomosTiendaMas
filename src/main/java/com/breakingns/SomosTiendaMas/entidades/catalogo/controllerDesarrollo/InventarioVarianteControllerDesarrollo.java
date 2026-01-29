package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMovimientoInventarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioVarianteControllerDesarrollo {

    private final IInventarioVarianteService service;
    private final IMovimientoInventarioService movimientoService;

    public InventarioVarianteControllerDesarrollo(
            IInventarioVarianteService service,
            IMovimientoInventarioService movimientoService) {
        this.service = service;
        this.movimientoService = movimientoService;
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

    @PostMapping("/confirmar")
    public ResponseEntity<OperacionSimpleResponseDTO> confirmarVenta(@RequestBody @Valid ConfirmacionVentaRequestDTO request) {
        OperacionSimpleResponseDTO resp = service.confirmarVenta(request);
        return ResponseEntity.ok(resp);
    }

    // ========== ENDPOINTS ESPECÍFICOS POR VARIANTE ==========

    /**
     * Ajuste manual de stock (delta + / -)
     * POST /api/inventario/variantes/{id}/ajuste
     */
    @PostMapping("/variantes/{varianteId}/ajuste")
    public ResponseEntity<InventarioVarianteDTO> ajustarStockPorVariante(
            @PathVariable Long varianteId,
            @RequestParam(required = false, defaultValue = "0") long deltaOnHand,
            @RequestParam(required = false, defaultValue = "0") long deltaReserved) {
        InventarioVarianteDTO dto = service.ajustarStock(varianteId, deltaOnHand, deltaReserved);
        return ResponseEntity.ok(dto);
    }

    /**
     * Reserva stock para carrito/checkout
     * POST /api/inventario/variantes/{id}/reserva
     * Body: { "cantidad": 2, "orderRef": "ORD-123" }
     */
    @PostMapping("/variantes/{varianteId}/reserva")
    public ResponseEntity<ReservaStockResponseDTO> reservarPorVariante(
            @PathVariable Long varianteId,
            @RequestBody @Valid ReservaStockRequestDTO request) {
        request.setVarianteId(varianteId);
        ReservaStockResponseDTO resp = service.reservarStock(request);
        return ResponseEntity.ok(resp);
    }

    /**
     * Libera reserva de stock
     * POST /api/inventario/variantes/{id}/liberar
     * Body: { "orderRef": "ORD-123" }
     */
    @PostMapping("/variantes/{varianteId}/liberar")
    public ResponseEntity<OperacionSimpleResponseDTO> liberarPorVariante(
            @PathVariable Long varianteId,
            @RequestBody @Valid LiberacionReservaRequestDTO request) {
        // el servicio usa orderRef; varianteId es para consistencia de ruta
        OperacionSimpleResponseDTO resp = service.liberarReserva(request);
        return ResponseEntity.ok(resp);
    }

    /**
     * Estado de stock: onhand, reserved, available
     * GET /api/inventario/variantes/{id}/estado
     */
    @GetMapping("/variantes/{varianteId}/estado")
    public ResponseEntity<DisponibilidadResponseDTO> estadoPorVariante(@PathVariable Long varianteId) {
        DisponibilidadResponseDTO resp = service.disponibilidad(varianteId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Historial de movimientos de stock por variante
     * GET /api/inventario/variantes/{id}/movimientos
     */
    @GetMapping("/variantes/{varianteId}/movimientos")
    public ResponseEntity<List<MovimientoResumenDTO>> movimientosPorVariante(@PathVariable Long varianteId) {
        List<MovimientoResumenDTO> list = movimientoService.listarPorVarianteId(varianteId);
        return ResponseEntity.ok(list);
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