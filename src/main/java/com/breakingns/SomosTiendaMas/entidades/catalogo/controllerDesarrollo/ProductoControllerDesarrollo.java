package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoListaDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dev/api/productos")
public class ProductoControllerDesarrollo {

    private final IProductoService productoService;

    public ProductoControllerDesarrollo(IProductoService productoService) {
        this.productoService = productoService;
    }

    private ProductoListaDTO toLista(ProductoResponseDTO r) {
        if (r == null) return null;
        ProductoListaDTO dto = new ProductoListaDTO();
        dto.setId(r.getId());
        dto.setNombre(r.getNombre());
        dto.setSlug(r.getSlug());
        dto.setMarcaId(r.getMarcaId());
        dto.setCategoriaId(r.getIdCategoriaHija());
        dto.setSku(r.getSku());
            dto.setCondicion(r.getCondicion());
            dto.setSkuResuelto(r.getSkuResuelto());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<ProductoListaDTO>> listar() {
        List<ProductoListaDTO> list = productoService.listarActivas()
                .stream()
                .map(this::toLista)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            ProductoResponseDTO r = productoService.obtenerPorId(id);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> obtenerPorSlug(@PathVariable String slug) {
        try {
            ProductoResponseDTO r = productoService.obtenerPorSlug(slug);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoCrearDTO dto) {
        ProductoResponseDTO created = productoService.crear(dto);
        URI location = URI.create("/dev/api/productos/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoActualizarDTO dto) {
        try {
            ProductoResponseDTO updated = productoService.actualizar(id, dto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }
}
