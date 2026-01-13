/* package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionValorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/valores")
public class OpcionValorController {

    private final IOpcionValorService service;

    public OpcionValorController(IOpcionValorService service) {
        this.service = service;
    }

    // --- CRUD genérico de valores (base: /api/valores) ---

    @PostMapping
    public ResponseEntity<OpcionValorResponseDTO> crear(@Valid @RequestBody OpcionValorCrearDTO dto,
                                                        UriComponentsBuilder uriBuilder) {
        OpcionValorResponseDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/valores/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpcionValorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OpcionValorResponseDTO> obtenerPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.obtenerPorSlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<OpcionValorResponseDTO>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpcionValorResponseDTO> actualizar(@PathVariable Long id,
                                                             @Valid @RequestBody OpcionValorActualizarDTO dto) {
        OpcionValorResponseDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
*/