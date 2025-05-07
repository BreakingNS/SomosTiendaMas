package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registro")
public class RegistroController {
    
    private final UsuarioServiceImpl usuarioService;

    public RegistroController(UsuarioServiceImpl usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    @PostMapping("/public/usuario")
    public ResponseEntity<String> registerUser(@RequestBody RegistroUsuarioDTO registroDTO) {
        usuarioService.registrarConRolDesdeDTO(registroDTO, RolNombre.ROLE_USUARIO);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }
    
    /*
    @PostMapping("/public/usuario")
    public ResponseEntity<String> registerUser(@RequestBody Usuario usuario) {
        usuarioService.registrarConRol(usuario, RolNombre.ROLE_USUARIO);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }
    */
    @PostMapping("/public/sinrol") // SOLO PRUEBA, no produccion
    public ResponseEntity<String> registerUserSinRol(@RequestBody Usuario usuario) {
        usuarioService.registrarSinRol(usuario);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/public/admin") // SOLO PRUEBA, no produccion
    public ResponseEntity<String> registerAdmin(@RequestBody Usuario usuario) {
        usuarioService.registrarConRol(usuario, RolNombre.ROLE_ADMIN);
        return ResponseEntity.ok("Administrador registrado correctamente");
    }
    
}
