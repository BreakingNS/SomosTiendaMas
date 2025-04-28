package com.breakingns.SomosTiendaMas.domain.usuario.controller;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.model.RolNombre;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import com.breakingns.SomosTiendaMas.auth.service.RolService;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    
    @Autowired
    private RolService rolService;
    
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

    @PostMapping("/registro/usuario")
    public ResponseEntity<?> registerUser(@RequestBody Usuario usuario) {
        if (usuarioService.existeUsuario(usuario.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("El nombre de usuario ya está en uso");
        }

        // Asignar el rol por defecto
        Rol rolUser = rolService.getByNombre(RolNombre.ROLE_USUARIO)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
        usuario.getRoles().add(rolUser);

        usuarioService.registrar(usuario);
        
        crearCarrito(usuario.getIdUsuario());
        
        return ResponseEntity.ok("Usuario registrado correctamente");
    }
    
    @PostMapping("/registro/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Usuario usuario) {
        if (usuarioService.existeUsuario(usuario.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("El nombre de usuario ya está en uso");
        }

        // Asignar el rol por defecto
        Rol rolAdmin = rolService.getByNombre(RolNombre.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
        usuario.getRoles().add(rolAdmin);

        usuarioService.registrar(usuario);
        
        crearCarrito(usuario.getIdUsuario());

        return ResponseEntity.ok("Administrador registrado correctamente");
    }
    
    //------------ Funciones utiles
    
    public void crearCarrito(Long id_usuario){
        
        carritoService.crearCarrito(id_usuario);
        
    }
}
