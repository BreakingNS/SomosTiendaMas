package com.breakingns.SomosTiendaMas.entidades.empresa.controller;

import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.PerfilEmpresaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.service.PerfilEmpresaService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/perfil-empresa")
public class PerfilEmpresaController {

    @Autowired
    private PerfilEmpresaService perfilEmpresaService;

    // Crear: opcional si registrador central lo hace (si se mantiene, devolver 201)
    @PreAuthorize("hasRole('ROLE_ADMIN')") // o permitir public si se mantiene endpoint público
    @PostMapping(path = "/public", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerfilEmpresaResponseDTO> registrarPerfilEmpresa(@RequestBody @Valid RegistroPerfilEmpresaDTO dto) {
        PerfilEmpresaResponseDTO response = perfilEmpresaService.registrarPerfilEmpresa(dto);
        return ResponseEntity.status(201).body(response);
    }

    // Obtener
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USUARIO')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerfilEmpresaResponseDTO> obtenerPerfilEmpresa(@PathVariable Long id) {
        return ResponseEntity.ok(perfilEmpresaService.obtenerPerfilEmpresa(id));
    }

    // Listar
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PerfilEmpresaResponseDTO>> listarPerfiles() {
        return ResponseEntity.ok(perfilEmpresaService.listarPerfiles());
    }

    // Actualización parcial
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USUARIO')")
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerfilEmpresaResponseDTO> patchPerfilEmpresa(@PathVariable Long id,
                                                                       @RequestBody @Valid ActualizarPerfilEmpresaDTO dto) {
        return ResponseEntity.ok(perfilEmpresaService.actualizarPerfilEmpresaParcial(id, dto));
    }

    // Eliminar
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPerfilEmpresa(@PathVariable Long id) {
        perfilEmpresaService.eliminarPerfilEmpresa(id);
        return ResponseEntity.noContent().build();
    }
}