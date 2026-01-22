package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/dev/api/variantes")
public class VarianteControllerDesarrollo {

    private final IVarianteService service;

    public VarianteControllerDesarrollo(IVarianteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VarianteDTO> crear(@RequestBody VarianteCrearDTO dto) {
        VarianteDTO created = service.crearVariante(dto);
        URI loc = URI.create("/dev/api/variantes/" + created.getId());
        return ResponseEntity.created(loc).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteDTO> obtener(@PathVariable Long id) {
        VarianteDTO dto = service.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<java.util.List<VarianteDTO>> listarPorProducto(@PathVariable Long productoId) {
        java.util.List<VarianteDTO> list = service.listarPorProductoId(productoId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VarianteDTO> actualizar(@PathVariable Long id, @RequestBody VarianteCrearDTO dto) {
        VarianteDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // REVIEW: modificar para que solo lo usen los admins
    // Endpoint temporal para borrado físico (uso solo para pruebas; restringir a admins luego)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> eliminarPermanente(@PathVariable Long id) {
        try {
            service.eliminarPermanente(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
    // batch create for product
    @PostMapping("/productos/{productoId}/variantes/batch")
    public ResponseEntity<List<VarianteDTO>> crearBatch(@PathVariable Long productoId, @RequestBody List<VarianteCrearDTO> dtos) {
        List<VarianteDTO> created = service.crearVariantesBatch(productoId, dtos);
        return ResponseEntity.ok(created);
    }*/
}
