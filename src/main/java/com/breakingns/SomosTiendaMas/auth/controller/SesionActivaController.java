package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sesiones")
@RequiredArgsConstructor
public class SesionActivaController {

    private final SesionActivaService sesionService;

    @GetMapping("/usuario/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public List<SesionActivaDTO> listarSesiones(@PathVariable Long id) {
        return sesionService.listarSesionesPorUsuario(id);
    }

    @PostMapping("/revocar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> revocarSesion(@RequestParam String token) {
        sesionService.revocarSesion(token);
        return ResponseEntity.ok("Sesión revocada");
    }
}