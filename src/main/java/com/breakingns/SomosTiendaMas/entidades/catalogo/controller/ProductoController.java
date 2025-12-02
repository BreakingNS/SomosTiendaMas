package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final IProductoService service;
    private final IProductoOpcionService productoOpcionService;

    public ProductoController(IProductoService service, IProductoOpcionService productoOpcionService) {
        this.service = service;
        this.productoOpcionService = productoOpcionService;
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCrearDTO dto, UriComponentsBuilder uriBuilder) {
        ProductoResponseDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/productos/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoActualizarDTO dto) {
        ProductoResponseDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.obtenerPorSlug(slug));
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarPorCategoriaId(categoriaId));
    }

    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<List<ProductoResponseDTO>> listarPorMarca(@PathVariable Long marcaId) {
        return ResponseEntity.ok(service.listarPorMarcaId(marcaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    //-----------

    // delega al service de opciones para devolver opciones + valores del producto
    @GetMapping("/{id}/con-opciones-valores-finales")
    public ResponseEntity<com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesValoresDTO> obtenerConOpcionesYValores(@PathVariable Long id) {
        var dto = productoOpcionService.obtenerProductoConOpcionesConValores(id);
        return ResponseEntity.ok(dto);
    }

    //-------------
}
