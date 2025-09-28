package com.breakingns.SomosTiendaMas.entidades.telefono.controller;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.ITelefonoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/telefono")
public class TelefonoController {

    @Autowired
    private ITelefonoService telefonoService;

    @PostMapping(path = "/public",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TelefonoResponseDTO> registrarTelefono(@RequestBody @Valid RegistroTelefonoDTO dto) {
        TelefonoResponseDTO response = telefonoService.registrarTelefono(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/private/{id}")
    public ResponseEntity<TelefonoResponseDTO> actualizarTelefono(@PathVariable Long id, @RequestBody ActualizarTelefonoDTO dto) {
        TelefonoResponseDTO response = telefonoService.actualizarTelefono(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<TelefonoResponseDTO> obtenerTelefono(@PathVariable Long id) {
        TelefonoResponseDTO response = telefonoService.obtenerTelefono(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/usuario/{idUsuario}")
    public ResponseEntity<List<TelefonoResponseDTO>> listarTelefonosPorUsuario(@PathVariable Long idUsuario) {
        List<TelefonoResponseDTO> response = telefonoService.listarTelefonosPorUsuario(idUsuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/empresa/{idPerfilEmpresa}")
    public ResponseEntity<List<TelefonoResponseDTO>> listarTelefonosPorPerfilEmpresa(@PathVariable Long idPerfilEmpresa) {
        List<TelefonoResponseDTO> response = telefonoService.listarTelefonosPorPerfilEmpresa(idPerfilEmpresa);
        return ResponseEntity.ok(response);
    }

    // Puedes agregar más endpoints según la lógica de negocio
}
