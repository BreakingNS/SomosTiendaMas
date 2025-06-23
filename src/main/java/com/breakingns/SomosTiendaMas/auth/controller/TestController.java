package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.request.OlvidePasswordRequest;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    private final AuthService authService;
    private final UsuarioServiceImpl usuarioService;

    public TestController(AuthService authService, UsuarioServiceImpl usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }    
    
    @PostMapping("/api/password/public/generartokenexpirado") // SOLO PRUEBAS
    public ResponseEntity<?> generarTokenExpirado(@Valid @RequestBody OlvidePasswordRequest request) {
        authService.generarTokenExpirado(request.email());
        return ResponseEntity.ok("Si el email existe, te enviaremos instrucciones para recuperar tu contraseña.");
    }
    
    @PostMapping("/api/password/public/generartokenusado") // SOLO PRUEBAS
    public ResponseEntity<?> generarTokenUsado(@Valid @RequestBody OlvidePasswordRequest request) {
        authService.generarTokenUsado(request.email());
        return ResponseEntity.ok("Si el email existe, te enviaremos instrucciones para recuperar tu contraseña.");
    }
    
    @PostMapping("/api/registro/public/sinrol") // SOLO PRUEBA, no produccion
    public ResponseEntity<String> registerUserSinRol(@RequestBody Usuario usuario) {
        usuarioService.registrarSinRol(usuario);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/api/registro/public/admin") // SOLO PRUEBA, no produccion
    public ResponseEntity<String> registerAdmin(@RequestBody Usuario usuario) {
        usuarioService.registrarConRol(usuario, RolNombre.ROLE_ADMIN);
        return ResponseEntity.ok("Administrador registrado correctamente");
    }
}
