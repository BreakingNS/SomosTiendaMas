package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoListaDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.ProductoPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public.ProductoCentralizadoPublicDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

// NOTE: Controlador de desarrollo para pruebas internas (CRUD básico).
@RestController
@RequestMapping("/dev/api/productos")
public class ProductoControllerDesarrollo {

    private final IProductoService productoService;

    public ProductoControllerDesarrollo(IProductoService productoService) {
        this.productoService = productoService;
    }

    private ProductoListaDTO toLista(ProductoCentralizadoResponseDTO r) {
        if (r == null) return null;
        ProductoListaDTO dto = new ProductoListaDTO();
        dto.setId(r.getId());
        dto.setNombre(r.getNombre());
        dto.setSlug(r.getSlug());
        dto.setMarcaId(r.getMarcaId());
        dto.setCategoriaId(r.getIdCategoriaHija());
        dto.setSku(r.getSku());
        dto.setCondicion(r.getCondicion());
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
            ProductoCentralizadoResponseDTO r = productoService.obtenerPorId(id);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @GetMapping("/{id}/public")
    public ResponseEntity<ProductoPublicDTO> obtenerPublicPorId(@PathVariable Long id) {
        try {
            ProductoCentralizadoResponseDTO r = productoService.obtenerPorId(id);
            if (r == null) return ResponseEntity.notFound().build();

            ProductoPublicDTO out = new ProductoPublicDTO();
            out.setNombre(r.getNombre());
            out.setSlug(r.getSlug());
            out.setDescripcion(r.getDescripcion());
            out.setMarcaNombre(r.getMarcaNombre());
            out.setNombreCategoriaPadre(r.getNombreCategoriaPadre());
            out.setNombreCategoriaHija(r.getNombreCategoriaHija());
            out.setSku(r.getSku());
            out.setCondicion(r.getCondicion() != null ? r.getCondicion().name() : null);
            out.setGarantia(r.getGarantia());
            out.setPoliticaDevoluciones(r.getPoliticaDevoluciones());

            return ResponseEntity.ok(out);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}/public-centralizado")
    public ResponseEntity<ProductoCentralizadoPublicDTO> obtenerPublicCentralizadoPorId(@PathVariable Long id) {
        try {
            ProductoCentralizadoResponseDTO r = productoService.obtenerPorId(id);
            if (r == null) return ResponseEntity.notFound().build();

            ProductoCentralizadoPublicDTO out = new ProductoCentralizadoPublicDTO();
            out.setId(r.getId());
            out.setNombre(r.getNombre());
            out.setSlug(r.getSlug());
            out.setDescripcion(r.getDescripcion());
            out.setMarcaNombre(r.getMarcaNombre());
            out.setNombreCategoriaPadre(r.getNombreCategoriaPadre());
            out.setNombreCategoriaHija(r.getNombreCategoriaHija());
            out.setSku(r.getSku());
            out.setCondicion(r.getCondicion() != null ? r.getCondicion().name() : null);
            out.setGarantia(r.getGarantia());
            out.setPoliticaDevoluciones(r.getPoliticaDevoluciones());
            
            return ResponseEntity.ok(out);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> obtenerPorSlug(@PathVariable String slug) {
        try {
            ProductoCentralizadoResponseDTO r = productoService.obtenerPorSlug(slug);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoCrearDTO dto) {
        // Este controlador solo crea el producto (sin variantes). Limpiamos
        // cualquier variante por defecto que venga en el DTO y usamos
        // el servicio especializado crearSoloProducto.
        dto.setVarianteDefault(null);
        ProductoCentralizadoResponseDTO created = productoService.crearSoloProducto(dto);
        URI location = URI.create("/dev/api/productos/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoActualizarDTO dto) {
        try {
            ProductoCentralizadoResponseDTO updated = productoService.actualizar(id, dto);
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

    // REVIEW: modificar para que solo lo usen los admins
    // Endpoint temporal para borrado físico (uso solo para pruebas; restringir a admins luego)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> eliminarPermanente(@PathVariable Long id) {
        try {
            productoService.eliminarPermanente(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar permanentemente: " + ex.getMessage());
        }
    }
}
