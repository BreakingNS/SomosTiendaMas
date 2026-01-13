/* 
package com.breakingns.SomosTiendaMas.entidades.catalogo.controller12345;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.servicios.catalogo.ImagenProductoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogo/imagenes")
public class ImagenProductoController {

    private final ImagenProductoService service;

    public ImagenProductoController(ImagenProductoService service) {
        this.service = service;
    }

    @PostMapping(path = "/producto/{productoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenVarianteDTO> uploadForProducto(
            @PathVariable Long productoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "alt", required = false) String alt,
            @RequestParam(value = "orden", required = false) Integer orden
    ) {
        return ResponseEntity.ok(service.uploadForProducto(productoId, file, alt, orden));
    }

    @PostMapping(path = "/variante/{varianteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenVarianteDTO> uploadForVariante(
            @PathVariable Long varianteId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "alt", required = false) String alt,
            @RequestParam(value = "orden", required = false) Integer orden
    ) {
        return ResponseEntity.ok(service.uploadForVariante(varianteId, file, alt, orden));
    }

    @GetMapping("/producto/{productoId}")
    public List<ImagenVarianteDTO> listByProducto(@PathVariable Long productoId) {
        return service.listByProducto(productoId);
    }

    @GetMapping("/variante/{varianteId}")
    public List<ImagenVarianteDTO> listByVariante(@PathVariable Long varianteId) {
        return service.listByVariante(varianteId);
    }

    @DeleteMapping("/{imagenId}")
    public ResponseEntity<Void> delete(@PathVariable Long imagenId) {
        service.deleteImagen(imagenId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imagenId}/orden")
    public ImagenVarianteDTO updateOrden(@PathVariable Long imagenId, @RequestBody Map<String, Integer> body) {
        return service.updateOrden(imagenId, body.get("orden"));
    }

    @PatchMapping("/{imagenId}/alt")
    public ImagenVarianteDTO updateAlt(@PathVariable Long imagenId, @RequestBody Map<String, String> body) {
        return service.updateAlt(imagenId, body.get("alt"));
    }
}
*/