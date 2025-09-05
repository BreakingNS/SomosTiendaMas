package com.breakingns.SomosTiendaMas.entidades.usuario.controller;

import com.breakingns.SomosTiendaMas.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    @Autowired
    private CarritoService carritoService;
    /*
    @Autowired
    private PasswordEncoder passwordEncoder;
    */

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> soloAdmin() {
        return ResponseEntity.ok("Hola Admin!");
    }

    @PreAuthorize("hasRole('ROLE_USUARIO')")
    @GetMapping("/comun")
    public ResponseEntity<String> soloUsuario() {
        return ResponseEntity.ok("Hola usuario!");
    }
    
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USUARIO')")
    @GetMapping("/compartido")
    public ResponseEntity<String> adminYUsuarios() {
        return ResponseEntity.ok("Hola usuarios!");
    }
    
    //------------ Funciones utiles
    
    public void crearCarrito(Long id_usuario){
        
        carritoService.crearCarrito(id_usuario);
        
    }
}
