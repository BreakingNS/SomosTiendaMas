package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.MarcaActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.MarcaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca.MarcaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.MarcaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMarcaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo/marcas")
public class MarcaController {

    private final IMarcaService marcaService;

    public MarcaController(IMarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @PostMapping
    public ResponseEntity<MarcaResponseDTO> crear(@RequestBody @Valid MarcaCrearDTO dto) {
        Marca m = new Marca();
        m.setNombre(dto.getNombre());
        m.setSlug(dto.getSlug());
        m.setDescripcion(dto.getDescripcion());
        Marca creado = marcaService.crear(m);
        return ResponseEntity.ok(MarcaMapper.toResponse(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid MarcaActualizarDTO dto) {
        Marca cambios = new Marca();
        cambios.setNombre(dto.getNombre());
        cambios.setSlug(dto.getSlug());
        cambios.setDescripcion(dto.getDescripcion());
        Marca actualizado = marcaService.actualizar(id, cambios);
        return ResponseEntity.ok(MarcaMapper.toResponse(actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> obtener(@PathVariable Long id) {
        Marca m = marcaService.obtener(id).orElseThrow();
        return ResponseEntity.ok(MarcaMapper.toResponse(m));
    }

    @GetMapping
    public ResponseEntity<List<MarcaResponseDTO>> listar() {
        List<MarcaResponseDTO> list = marcaService.listar().stream().map(MarcaMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam String usuario) {
        marcaService.eliminarLogico(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
