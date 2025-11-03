package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OpcionController {

    private final IOpcionService service;

    public OpcionController(IOpcionService service) {
        this.service = service;
    }

    // ---- OPCIONES ----

    @PostMapping("/productos/{productoId}/opciones")
    public ResponseEntity<OpcionResponseDTO> crearParaProducto(@PathVariable Long productoId,
                                                               @Valid @RequestBody OpcionCrearDTO dto,
                                                               UriComponentsBuilder uriBuilder) {
        if (dto.getProductoId() == null) dto.setProductoId(productoId);
        OpcionResponseDTO created = service.crearOpcion(dto);
        URI location = uriBuilder.path("/api/opciones/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/opciones")
    public ResponseEntity<OpcionResponseDTO> crear(@Valid @RequestBody OpcionCrearDTO dto, UriComponentsBuilder uriBuilder) {
        OpcionResponseDTO created = service.crearOpcion(dto);
        URI location = uriBuilder.path("/api/opciones/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/opciones/{id}")
    public ResponseEntity<OpcionResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody OpcionActualizarDTO dto) {
        OpcionResponseDTO updated = service.actualizarOpcion(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/opciones/{id}")
    public ResponseEntity<OpcionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerOpcionPorId(id));
    }

    @GetMapping("/productos/{productoId}/opciones")
    public ResponseEntity<List<OpcionResumenDTO>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(service.listarOpcionesPorProductoId(productoId));
    }

    @DeleteMapping("/opciones/{id}")
    public ResponseEntity<Void> eliminarOpcion(@PathVariable Long id) {
        service.eliminarOpcion(id);
        return ResponseEntity.noContent().build();
    }

    // ---- VALORES DE OPCIÓN (solo endpoints por opción) ----
    // Los endpoints genéricos CRUD sobre /api/valores (GET/PUT/DELETE by id, POST genérico)
    // deben residir exclusivamente en OpcionValorController para evitar mappings duplicados.

    @PostMapping("/opciones/{opcionId}/valores")
    public ResponseEntity<OpcionValorResponseDTO> crearValorParaOpcion(@PathVariable Long opcionId,
                                                                       @Valid @RequestBody OpcionValorCrearDTO dto,
                                                                       UriComponentsBuilder uriBuilder) {
        if (dto.getOpcionId() == null) dto.setOpcionId(opcionId);
        OpcionValorResponseDTO created = service.crearValor(dto);
        URI location = uriBuilder.path("/api/valores/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/opciones/{opcionId}/valores")
    public ResponseEntity<List<OpcionValorResponseDTO>> listarValoresPorOpcion(@PathVariable Long opcionId) {
        return ResponseEntity.ok(service.listarValoresPorOpcionId(opcionId));
    }

}