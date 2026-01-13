package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenVarianteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/dev/api")
public class ImagenVarianteControllerDesarrollo {

    private final IImagenVarianteService service;

    public ImagenVarianteControllerDesarrollo(IImagenVarianteService service) {
        this.service = service;
    }

    // Crear imagen asociada a una variante
    @PostMapping(value = "/variantes/{varianteId}/imagenes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImagenVarianteDTO>> uploadParaVariante(
            @PathVariable Long varianteId,
            @RequestParam("files") MultipartFile[] files,
            UriComponentsBuilder uriBuilder) {

        // Implementá service.uploadAndCreate(varianteId, files)
        List<ImagenVarianteDTO> created = service.uploadAndCreate(varianteId, files);
        URI location = uriBuilder.path("/api/variantes/{varianteId}/imagenes").buildAndExpand(varianteId).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Crear imagen independiente (si prefieres enviar varianteId en body)
    @PostMapping("/imagenes")
    public ResponseEntity<ImagenVarianteDTO> crear(@Valid @RequestBody ImagenVarianteDTO dto, UriComponentsBuilder uriBuilder) {
        ImagenVarianteDTO created = service.crear(dto);
        URI location = uriBuilder.path("/api/imagenes/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/imagenes/{id}")
    public ResponseEntity<ImagenVarianteDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ImagenVarianteDTO dto) {
        ImagenVarianteDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/imagenes/{id}")
    public ResponseEntity<ImagenVarianteDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping("/variantes/{varianteId}/imagenes")
    public ResponseEntity<List<ImagenVarianteDTO>> listarPorVariante(@PathVariable Long varianteId) {
        return ResponseEntity.ok(service.listarPorVarianteId(varianteId));
    }

    @GetMapping("/variantes/{varianteId}/imagenes/primera")
    public ResponseEntity<ImagenVarianteDTO> obtenerPrimeraPorVariante(@PathVariable Long varianteId) {
        ImagenVarianteDTO dto = service.obtenerPrimeraPorVarianteId(varianteId);
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/imagenes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/variantes/{varianteId}/imagenes")
    public ResponseEntity<Void> eliminarPorVariante(@PathVariable Long varianteId) {
        service.eliminarPorVarianteId(varianteId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/variantes/{varianteId}/imagenes/reordenar")
    public ResponseEntity<Void> reordenar(@PathVariable Long varianteId, @RequestBody List<Long> imagenIdsOrdenados) {
        service.reordenarPorVariante(varianteId, imagenIdsOrdenados);
        return ResponseEntity.ok().build();
    }
}