/*
package com.breakingns.SomosTiendaMas.entidades.catalogo.controller12345;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class OpcionController {

    private final IOpcionService service;

    public OpcionController(IOpcionService service) {
        this.service = service;
    }

    // Opciones
    @PostMapping("/productos/{productoId}/opciones")
    public ResponseEntity<OpcionResponseDTO> crearOpcion(@PathVariable Long productoId,
                                                                 @RequestBody @Valid OpcionActualizarDTO dto) {
        Opcion o = service.crearOpcion(productoId, dto.getNombre(), dto.getOrden());
        return ResponseEntity.ok(OpcionMapper.toResponse(o));
    }

    @PutMapping("/opciones/{opcionId}")
    public ResponseEntity<OpcionResponseDTO> actualizarOpcion(@PathVariable Long opcionId,
                                                                      @RequestBody @Valid OpcionActualizarDTO dto) {
        Opcion o = service.actualizarOpcion(opcionId, dto.getNombre(), dto.getOrden());
        return ResponseEntity.ok(OpcionMapper.toResponse(o));
    }

    @GetMapping("/productos/{productoId}/opciones")
    public ResponseEntity<List<OpcionResponseDTO>> listarOpciones(@PathVariable Long productoId) {
        List<OpcionResponseDTO> list = service.listarOpciones(productoId)
                .stream().map(OpcionMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/opciones/{opcionId}")
    public ResponseEntity<Void> eliminarOpcion(@PathVariable Long opcionId) {
        service.eliminarOpcion(opcionId);
        return ResponseEntity.noContent().build();
    }

    // Valores
    @PostMapping("/opciones/{opcionId}/valores")
    public ResponseEntity<OpcionValorResponseDTO> crearValor(@PathVariable Long opcionId,
                                                                     @RequestBody @Valid OpcionValorCrearDTO dto) {
        OpcionValor v = service.crearValor(opcionId, dto.getValor(), dto.getSlug(), dto.getOrden());
        return ResponseEntity.ok(OpcionValorMapper.toResponse(v));
    }

    @GetMapping("/opciones/{opcionId}/valores")
    public ResponseEntity<List<OpcionValorResponseDTO>> listarValores(@PathVariable Long opcionId) {
        List<OpcionValorResponseDTO> list = service.listarValores(opcionId)
                .stream().map(OpcionValorMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/valores/{valorId}")
    public ResponseEntity<Void> eliminarValor(@PathVariable Long valorId) {
        service.eliminarValor(valorId);
        return ResponseEntity.noContent().build();
    }
}
     */
