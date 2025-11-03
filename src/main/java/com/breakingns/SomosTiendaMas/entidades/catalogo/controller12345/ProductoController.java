/*
package com.breakingns.SomosTiendaMas.entidades.catalogo.controller12345;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo/productos")
public class ProductoController {

    private final IProductoService productoService;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(IProductoService productoService,
                              MarcaRepository marcaRepository,
                              CategoriaRepository categoriaRepository) {
        this.productoService = productoService;
        this.marcaRepository = marcaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@RequestBody @Valid ProductoCrearDTO dto) {
        Marca marca = dto.getMarcaId() != null ? marcaRepository.findById(dto.getMarcaId()).orElse(null) : null;
        Categoria categoria = dto.getCategoriaId() != null ? categoriaRepository.findById(dto.getCategoriaId()).orElse(null) : null;
        Producto p = ProductoMapper.toEntity(dto, marca, categoria);
        Producto creado = productoService.crear(p);
        return ResponseEntity.ok(ProductoMapper.toResponse(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid ProductoActualizarDTO dto) {
        Producto cambios = new Producto();
        cambios.setNombre(dto.getNombre());
        cambios.setSlug(dto.getSlug());
        cambios.setDescripcion(dto.getDescripcion());
        if (dto.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(dto.getMarcaId()).orElse(null);
            cambios.setMarca(marca);
        }
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId()).orElse(null);
            cambios.setCategoria(categoria);
        }
        cambios.setMetadataJson(dto.getMetadataJson());

        Producto actualizado = productoService.actualizar(id, cambios);
        return ResponseEntity.ok(ProductoMapper.toResponse(actualizado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtener(@PathVariable Long id) {
        Producto p = productoService.obtener(id).orElseThrow();
        return ResponseEntity.ok(ProductoMapper.toResponse(p));
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        List<ProductoResponseDTO> list = productoService.listar().stream()
                .map(ProductoMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam String usuario) {
        productoService.eliminarLogico(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
 */