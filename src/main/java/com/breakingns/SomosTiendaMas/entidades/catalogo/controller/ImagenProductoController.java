/*
/* package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ImagenProductoController {

    private final IImagenProductoService service;

    public ImagenProductoController(IImagenProductoService service) {
        this.service = service;
    }

    // Crear imagen asociada a un producto
    @PostMapping(value = "/productos/{productoId}/imagenes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImagenProductoDTO>> uploadParaProducto(
            @PathVariable Long productoId,
            @RequestParam("files") MultipartFile[] files,
            UriComponentsBuilder uriBuilder) {

        // Implementá service.uploadAndCreate(productoId, files)
        List<ImagenProductoDTO> created = service.uploadAndCreate(productoId, files);
        URI location = uriBuilder.path("/api/productos/{productoId}/imagenes").buildAndExpand(productoId).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Crear imagen independiente (si prefieres enviar productoId en body)
    @PostMapping("/imagenes")
    public ResponseEntity<ImagenProductoDTO> crear(@Valid @RequestBody ImagenProductoDTO dto, UriComponentsBuilder uriBuilder) {
        ImagenProductoDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/imagenes/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/imagenes/{id}")
    public ResponseEntity<ImagenProductoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ImagenProductoDTO dto) {
        ImagenProductoDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/imagenes/{id}")
    public ResponseEntity<ImagenProductoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/productos/{productoId}/imagenes")
    public ResponseEntity<List<ImagenProductoDTO>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(service.listarPorProductoId(productoId));
    }

    @GetMapping("/productos/{productoId}/imagenes/primera")
    public ResponseEntity<ImagenProductoDTO> obtenerPrimeraPorProducto(@PathVariable Long productoId) {
        ImagenProductoDTO dto = service.obtenerPrimeraPorProductoId(productoId);
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/imagenes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/productos/{productoId}/imagenes")
    public ResponseEntity<Void> eliminarPorProducto(@PathVariable Long productoId) {
        service.eliminarPorProductoId(productoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/productos/{productoId}/imagenes/reordenar")
    public ResponseEntity<Void> reordenar(@PathVariable Long productoId, @RequestBody List<Long> imagenIdsOrdenados) {
        service.reordenarPorProducto(productoId, imagenIdsOrdenados);
        return ResponseEntity.ok().build();
    }
}
 */