package com.breakingns.SomosTiendaMas.entidades.telefono.controller;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.*;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.ITelefonoService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/telefono")
public class TelefonoController {

    private final ITelefonoService telefonoService;

    public TelefonoController(ITelefonoService telefonoService) {
        this.telefonoService = telefonoService;
    }

    @PostMapping(path = "/public/registrar",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TelefonoResponseDTO> registrar(@RequestBody @Valid RegistroTelefonoDTO dto) {
        return ResponseEntity.ok(telefonoService.registrarTelefono(dto));
    }

    @PostMapping(path = "/public/registrar-multiple",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TelefonoResponseDTO>> registrarMultiple(@RequestBody List<@Valid RegistroTelefonoDTO> dtos) {
        List<TelefonoResponseDTO> res = dtos.stream()
                .map(telefonoService::registrarTelefono)
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @PutMapping(path = "/private/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TelefonoResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid ActualizarTelefonoDTO dto) {
        return ResponseEntity.ok(telefonoService.actualizarTelefono(id, dto));
    }

    @GetMapping(path = "/private/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TelefonoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(telefonoService.obtenerTelefono(id));
    }

    @GetMapping(path = "/private/usuario/{perfilUsuarioId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TelefonoResponseDTO>> listarPorUsuario(@PathVariable Long perfilUsuarioId) {
        return ResponseEntity.ok(telefonoService.listarTelefonosPorUsuario(perfilUsuarioId));
    }

    @GetMapping(path = "/private/empresa/{perfilEmpresaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TelefonoResponseDTO>> listarPorEmpresa(@PathVariable Long perfilEmpresaId) {
        return ResponseEntity.ok(telefonoService.listarTelefonosPorPerfilEmpresa(perfilEmpresaId));
    }
}
