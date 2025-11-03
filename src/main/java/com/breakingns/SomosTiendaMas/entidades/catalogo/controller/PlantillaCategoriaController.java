package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPlantillaCategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/plantillas")
public class PlantillaCategoriaController {

    private final IPlantillaCategoriaService service;

    public PlantillaCategoriaController(IPlantillaCategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PlantillaCategoriaResponseDTO> crear(@Valid @RequestBody PlantillaCategoriaCrearDTO dto,
                                                               UriComponentsBuilder uriBuilder) {
        PlantillaCategoriaResponseDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/plantillas/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantillaCategoriaResponseDTO> actualizar(@PathVariable Long id,
                                                                     @Valid @RequestBody PlantillaCategoriaActualizarDTO dto) {
        PlantillaCategoriaResponseDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaCategoriaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<PlantillaCategoriaResumenDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarPorCategoriaId(categoriaId));
    }

    @GetMapping
    public ResponseEntity<List<PlantillaCategoriaResponseDTO>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<PlantillaCategoriaResponseDTO> obtenerPorCategoriaYNombre(
            @RequestParam Long categoriaId,
            @RequestParam String nombre) {
        return ResponseEntity.ok(service.obtenerPorCategoriaIdYNombre(categoriaId, nombre));
    }
}
