package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IEtiquetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final IEtiquetaService service;

    public EtiquetaController(IEtiquetaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EtiquetaResponseDTO> crear(@Valid @RequestBody EtiquetaCrearDTO dto, UriComponentsBuilder uriBuilder) {
        EtiquetaResponseDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/etiquetas/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EtiquetaResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody EtiquetaActualizarDTO dto) {
        EtiquetaResponseDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtiquetaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<EtiquetaResponseDTO> obtenerPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.obtenerPorSlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<EtiquetaResumenDTO>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
