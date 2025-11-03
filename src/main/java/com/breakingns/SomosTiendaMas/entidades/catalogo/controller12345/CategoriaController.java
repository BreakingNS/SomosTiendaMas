/*
package com.breakingns.SomosTiendaMas.entidades.catalogo.controller12345;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria.CategoriaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.CategoriaMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.ICategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo/categorias")
public class CategoriaController {

    private final ICategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    public CategoriaController(ICategoriaService categoriaService, CategoriaRepository categoriaRepository) {
        this.categoriaService = categoriaService;
        this.categoriaRepository = categoriaRepository;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(@RequestBody @Valid CategoriaCrearDTO dto) {
        Categoria c = new Categoria();
        c.setNombre(dto.getNombre());
        c.setSlug(dto.getSlug());
        c.setDescripcion(dto.getDescripcion());
        if (dto.getCategoriaPadreId() != null) {
            categoriaRepository.findById(dto.getCategoriaPadreId()).ifPresent(c::setCategoriaPadre);
        }
        Categoria creado = categoriaService.crear(c);
        return ResponseEntity.ok(CategoriaMapper.toResponse(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid CategoriaActualizarDTO dto) {
        Categoria cambios = new Categoria();
        cambios.setNombre(dto.getNombre());
        cambios.setSlug(dto.getSlug());
        cambios.setDescripcion(dto.getDescripcion());
        if (dto.getCategoriaPadreId() != null) {
            categoriaRepository.findById(dto.getCategoriaPadreId()).ifPresent(cambios::setCategoriaPadre);
        }
        Categoria actualizado = categoriaService.actualizar(id, cambios);
        return ResponseEntity.ok(CategoriaMapper.toResponse(actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtener(@PathVariable Long id) {
        Categoria c = categoriaService.obtener(id).orElseThrow();
        return ResponseEntity.ok(CategoriaMapper.toResponse(c));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        List<CategoriaResponseDTO> list = categoriaService.listar().stream()
                .map(CategoriaMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam String usuario) {
        categoriaService.eliminarLogico(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
*/