package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/dev/api")
public class VarianteControllerDesarrollo {

    private final IVarianteService service;

    public VarianteControllerDesarrollo(IVarianteService service) {
        this.service = service;
    }

    @PostMapping("/variantes")
    public ResponseEntity<VarianteDTO> crear(@RequestBody VarianteCrearDTO dto) {
        VarianteDTO created = service.crearVariante(dto);
        URI loc = URI.create("/dev/api/variantes/" + created.getId());
        return ResponseEntity.created(loc).body(created);
    }

    @GetMapping("/variantes/{id}")
    public ResponseEntity<VarianteDTO> obtener(@PathVariable Long id) {
        VarianteDTO dto = service.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/variantes/{id}")
    public ResponseEntity<VarianteDTO> actualizar(@PathVariable Long id, @RequestBody VarianteCrearDTO dto) {
        VarianteDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/variantes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // batch create for product
    @PostMapping("/productos/{productoId}/variantes/batch")
    public ResponseEntity<List<VarianteDTO>> crearBatch(@PathVariable Long productoId, @RequestBody List<VarianteCrearDTO> dtos) {
        List<VarianteDTO> created = service.crearVariantesBatch(productoId, dtos);
        return ResponseEntity.ok(created);
    }
}
