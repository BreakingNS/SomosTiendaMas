package com.breakingns.SomosTiendaMas.entidades.usuario.controller;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.ActualizarUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.IUsuarioService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final IUsuarioService usuarioService; // usar interfaz

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // READ: obtener por id (cualquier usuario autenticado o admin según política)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USUARIO')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.consultarUsuario(id));
    }

    // LIST: admins pueden listar usuarios
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // UPDATE parcial (PATCH) — los campos nulos no se sobrescriben
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USUARIO')")
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponseDTO> patchUsuario(@PathVariable Long id,
                                                           @RequestBody @Valid ActualizarUsuarioDTO dto) {
        UsuarioResponseDTO updated = usuarioService.actualizarUsuarioParcial(id, dto);
        return ResponseEntity.ok(updated);
    }

    // DELETE (soft o hard según política)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // NOTA: la creación (registro) puede delegarse al RegistroController centralizado.

    // Comprobar disponibilidad de username (true = disponible)
    @GetMapping(path = "/public/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam("username") String username) {
        boolean exists = usuarioService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("available", !exists));
    }

    // Comprobar disponibilidad de email (true = disponible)
    @GetMapping(path = "/public/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        boolean exists = usuarioService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("available", !exists));
    }
}
