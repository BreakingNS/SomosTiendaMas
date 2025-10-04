package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.InventarioMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final IInventarioService inventarioService;

    public InventarioController(IInventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/variantes/{id}/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> disponibilidad(@PathVariable("id") Long varianteId) {
        long disp = inventarioService.disponibilidad(varianteId);
        return ResponseEntity.ok(InventarioMapper.toDisponibilidad(varianteId, disp));
    }

    @PostMapping("/reservas")
    public ResponseEntity<ReservaStockResponseDTO> reservar(@RequestBody @Valid ReservaStockRequestDTO req) {
        boolean ok = inventarioService.reservar(req.getVarianteId(), req.getCantidad(), req.getOrderRef());
        long disp = inventarioService.disponibilidad(req.getVarianteId());
        return ResponseEntity.ok(InventarioMapper.toReservaResponse(ok, disp));
    }

    @PostMapping("/confirmaciones")
    public ResponseEntity<OperacionSimpleResponseDTO> confirmar(@RequestBody @Valid ConfirmacionVentaRequestDTO req) {
        boolean ok = inventarioService.confirmar(req.getOrderRef());
        return ResponseEntity.ok(new OperacionSimpleResponseDTO(ok));
    }

    @PostMapping("/liberaciones")
    public ResponseEntity<OperacionSimpleResponseDTO> liberar(@RequestBody @Valid LiberacionReservaRequestDTO req) {
        boolean ok = inventarioService.liberar(req.getOrderRef());
        return ResponseEntity.ok(new OperacionSimpleResponseDTO(ok));
    }
}
