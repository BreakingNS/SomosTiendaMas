/* 
/* package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.ICategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ICategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@Valid @RequestBody CategoriaCrearDTO dto) {
        CategoriaResponseDTO created = categoriaService.crear(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaActualizarDTO dto) {
        CategoriaResponseDTO updated = categoriaService.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(@PathVariable Long id) {
        CategoriaResponseDTO resp = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorSlug(@PathVariable String slug) {
        CategoriaResponseDTO resp = categoriaService.obtenerPorSlug(slug);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResumenDTO>> listarActivas() {
        List<CategoriaResumenDTO> list = categoriaService.listarActivas();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/arbol")
    public ResponseEntity<List<CategoriaArbolDTO>> obtenerArbol() {
        List<CategoriaArbolDTO> tree = categoriaService.obtenerArbol();
        return ResponseEntity.ok(tree);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
*/