/*
package com.breakingns.SomosTiendaMas.entidades.catalogo.controller12345;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IEtiquetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo/etiquetas")
public class EtiquetaController {

    private final IEtiquetaService service;

    public EtiquetaController(IEtiquetaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Etiqueta> crear(@RequestBody @Valid Etiqueta dto) {
        Etiqueta creado = service.crear(dto);
        return ResponseEntity.ok(creado);
    }

    @GetMapping
    public ResponseEntity<List<Etiqueta>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Etiqueta> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id).orElseThrow());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Etiqueta> actualizar(@PathVariable Long id, @RequestBody @Valid Etiqueta cambios) {
        return ResponseEntity.ok(service.actualizar(id, cambios));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam String usuario) {
        service.eliminarLogico(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
*/