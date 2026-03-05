package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVendedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dev/api/vendedores")
@Validated
public class VendedorControllerDesarrollo {

    private final IVendedorService service;

    public VendedorControllerDesarrollo(IVendedorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VendedorResponseDTO> crear(@Valid @RequestBody VendedorCrearDTO dto) {
        VendedorResponseDTO resp = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendedorResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody VendedorActualizarDTO dto) {
        VendedorResponseDTO resp = service.actualizar(id, dto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendedorResponseDTO> obtenerPorId(@PathVariable Long id) {
        VendedorResponseDTO resp = service.obtenerPorId(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<VendedorResponseDTO> obtenerPorUserId(@PathVariable Long userId) {
        VendedorResponseDTO resp = service.obtenerPorUserId(userId);
        return ResponseEntity.ok(resp);
    }

    // --- Operaciones por userId (convenience endpoints) ---
    @PutMapping("/by-user/{userId}")
    public ResponseEntity<VendedorResponseDTO> actualizarPorUserId(@PathVariable Long userId, @Valid @RequestBody VendedorActualizarDTO dto) {
        VendedorResponseDTO resp = service.actualizarPorUserId(userId, dto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<VendedorResumenDTO>> listarActivos() {
        List<VendedorResumenDTO> list = service.listarActivos();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/search")
    public ResponseEntity<List<VendedorResumenDTO>> buscarPorFiltro(@RequestBody VendedorFiltroDTO filtro) {
        List<VendedorResumenDTO> list = service.buscarPorFiltro(filtro);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/by-user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPorUserId(@PathVariable Long userId) {
        service.eliminarPorUserId(userId);
    }

    @DeleteMapping("/by-user/{userId}/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purgarPorUserId(@PathVariable Long userId) {
        service.purgarPorUserId(userId);
    }

    // --- Operaciones por empresaId ---
    @GetMapping("/by-empresa/{empresaId}")
    public ResponseEntity<VendedorResponseDTO> obtenerPorEmpresaId(@PathVariable Long empresaId) {
        VendedorResponseDTO resp = service.obtenerPorEmpresaId(empresaId);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/by-empresa/{empresaId}")
    public ResponseEntity<VendedorResponseDTO> actualizarPorEmpresaId(@PathVariable Long empresaId, @Valid @RequestBody VendedorActualizarDTO dto) {
        VendedorResponseDTO resp = service.actualizarPorEmpresaId(empresaId, dto);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/by-empresa/{empresaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPorEmpresaId(@PathVariable Long empresaId) {
        service.eliminarPorEmpresaId(empresaId);
    }

    @DeleteMapping("/by-empresa/{empresaId}/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purgarPorEmpresaId(@PathVariable Long empresaId) {
        service.purgarPorEmpresaId(empresaId);
    }

}
