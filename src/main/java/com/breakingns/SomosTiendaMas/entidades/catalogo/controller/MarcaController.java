package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final IMarcaService marcaService;

    @PostMapping
    public ResponseEntity<MarcaResponseDTO> crear(@Valid @RequestBody MarcaCrearDTO dto) {
        MarcaResponseDTO created = marcaService.crear(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody MarcaActualizarDTO dto) {
        MarcaResponseDTO updated = marcaService.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> obtenerPorId(@PathVariable Long id) {
        MarcaResponseDTO resp = marcaService.obtenerPorId(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<MarcaResponseDTO> obtenerPorSlug(@PathVariable String slug) {
        MarcaResponseDTO resp = marcaService.obtenerPorSlug(slug);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<MarcaResumenDTO>> listarActivas() {
        List<MarcaResumenDTO> list = marcaService.listarActivas();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<MarcaResumenDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        List<MarcaResumenDTO> list = marcaService.listarPorCategoria(categoriaId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLogico(@PathVariable Long id, @RequestParam(required = false) String usuario) {
        marcaService.eliminarLogico(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
