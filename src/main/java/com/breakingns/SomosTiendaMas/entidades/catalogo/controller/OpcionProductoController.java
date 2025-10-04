package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ValorOpcionProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ValorOpcionProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class OpcionProductoController {

    private final IOpcionProductoService service;

    public OpcionProductoController(IOpcionProductoService service) {
        this.service = service;
    }

    // Opciones
    @PostMapping("/productos/{productoId}/opciones")
    public ResponseEntity<OpcionProductoResponseDTO> crearOpcion(@PathVariable Long productoId,
                                                                 @RequestBody @Valid OpcionProductoActualizarDTO dto) {
        OpcionProducto o = service.crearOpcion(productoId, dto.getNombre(), dto.getOrden());
        return ResponseEntity.ok(OpcionProductoMapper.toResponse(o));
    }

    @PutMapping("/opciones/{opcionId}")
    public ResponseEntity<OpcionProductoResponseDTO> actualizarOpcion(@PathVariable Long opcionId,
                                                                      @RequestBody @Valid OpcionProductoActualizarDTO dto) {
        OpcionProducto o = service.actualizarOpcion(opcionId, dto.getNombre(), dto.getOrden());
        return ResponseEntity.ok(OpcionProductoMapper.toResponse(o));
    }

    @GetMapping("/productos/{productoId}/opciones")
    public ResponseEntity<List<OpcionProductoResponseDTO>> listarOpciones(@PathVariable Long productoId) {
        List<OpcionProductoResponseDTO> list = service.listarOpciones(productoId)
                .stream().map(OpcionProductoMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/opciones/{opcionId}")
    public ResponseEntity<Void> eliminarOpcion(@PathVariable Long opcionId) {
        service.eliminarOpcion(opcionId);
        return ResponseEntity.noContent().build();
    }

    // Valores
    @PostMapping("/opciones/{opcionId}/valores")
    public ResponseEntity<ValorOpcionProductoResponseDTO> crearValor(@PathVariable Long opcionId,
                                                                     @RequestBody @Valid ValorOpcionProductoCrearDTO dto) {
        ValorOpcionProducto v = service.crearValor(opcionId, dto.getValor(), dto.getSlug(), dto.getOrden());
        return ResponseEntity.ok(ValorOpcionProductoMapper.toResponse(v));
    }

    @GetMapping("/opciones/{opcionId}/valores")
    public ResponseEntity<List<ValorOpcionProductoResponseDTO>> listarValores(@PathVariable Long opcionId) {
        List<ValorOpcionProductoResponseDTO> list = service.listarValores(opcionId)
                .stream().map(ValorOpcionProductoMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/valores/{valorId}")
    public ResponseEntity<Void> eliminarValor(@PathVariable Long valorId) {
        service.eliminarValor(valorId);
        return ResponseEntity.noContent().build();
    }
}
