package com.breakingns.SomosTiendaMas.controller;

import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.model.Usuario;
import com.breakingns.SomosTiendaMas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    /*
    @Autowired
    private PasswordEncoder passwordEncoder;
    */

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> soloAdmin() {
        return ResponseEntity.ok("Hola Admin!");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    @GetMapping("/comun")
    public ResponseEntity<String> adminYUsuarios() {
        return ResponseEntity.ok("Hola usuarios!");
    }

    
    @PostMapping("/registro")
    public ResponseEntity<?> registerUser(@RequestBody Usuario usuario) {
        if (usuarioService.existeUsuario(usuario.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("El nombre de usuario ya está en uso");
        }

        // Asignar el rol por defecto
        usuario.getRoles().add(RolNombre.ROLE_ADMIN);
        usuarioService.registrar(usuario);

        return ResponseEntity.ok("Usuario registrado correctamente");
    }
    
}
