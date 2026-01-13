package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteOpcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dev/api/variantes")
public class VarianteOpcionControllerDesarrollo {

    private final IVarianteOpcionService service;

    public VarianteOpcionControllerDesarrollo(IVarianteOpcionService service) {
        this.service = service;
    }

    @PostMapping("/{varianteId}/opciones/asignar")
    public ResponseEntity<VarianteConOpcionesValoresDTO> asignarOpciones(@PathVariable Long varianteId,
                                               @RequestBody VarianteOpcionesAsignarDTO dto,
                                               @RequestHeader(value = "X-User", required = false) String user) {
        dto.varianteId = varianteId;
        service.asignarOpciones(dto, user != null ? user : "system");
        var saved = service.obtenerVarianteConOpcionesConValores(varianteId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{varianteId}/con-opciones")
    public ResponseEntity<VarianteConOpcionesDTO> obtenerConOpciones(@PathVariable Long varianteId) {
        return ResponseEntity.ok(service.obtenerVarianteConOpciones(varianteId));
    }

    @GetMapping("/con-opciones")
    public ResponseEntity<List<VarianteConOpcionesDTO>> obtenerTodosConOpciones() {
        return ResponseEntity.ok(service.obtenerTodosConOpciones());
    }

    @GetMapping("/{varianteId}/con-opciones-valores")
    public ResponseEntity<VarianteConOpcionesValoresDTO> obtenerConOpcionesConValores(@PathVariable Long varianteId) {
        return ResponseEntity.ok(service.obtenerVarianteConOpcionesConValores(varianteId));
    }

    @GetMapping("/con-opciones-valores")
    public ResponseEntity<List<VarianteConOpcionesValoresDTO>> obtenerTodosConOpcionesConValores() {
        return ResponseEntity.ok(service.obtenerTodosConOpcionesConValores());
    }

    @PutMapping("/{varianteId}/opciones")
    public ResponseEntity<Void> modificarOpciones(@PathVariable Long varianteId,
                                                  @RequestBody VarianteOpcionesAsignarDTO dto,
                                                  @RequestHeader(value = "X-User", required = false) String user) {
        service.modificarOpciones(varianteId, dto, user != null ? user : "system");
        return ResponseEntity.ok().build();
    }
}