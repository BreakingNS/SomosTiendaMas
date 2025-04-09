package com.breakingns.SomosTiendaMas.controller;

import com.breakingns.SomosTiendaMas.config.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.model.Carrito;
import com.breakingns.SomosTiendaMas.model.Usuario;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import com.breakingns.SomosTiendaMas.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/*
public class CarritoController {
    
    @Autowired
    private CarritoService carritoService;
    
    @Autowired
    private UsuarioService usuarioService;
        
    @PreAuthorize("#id_usuario == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/carrito/{id_usuario}")
    public ResponseEntity<?> verCarrito(@PathVariable Long id_usuario) {
        Optional<Carrito> carrito = carritoService.traerCarritoPorIdUsuario(id_usuario);

        if (carrito.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(carrito.get());
    }
}*/

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/traer/{id_usuario}")
    public ResponseEntity<?> verCarrito(@PathVariable Long id_usuario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameActual = auth.getName(); // esto es el username del token

        // Obtener usuario actual desde la base de datos
        Usuario usuarioActual = usuarioService.findByUsername(usernameActual);

        // Si es admin, dejar pasar
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        // Si no es admin, solo puede ver su propio carrito
        if (!esAdmin && !usuarioActual.getId_usuario().equals(id_usuario)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
        }

        // OK, puede ver el carrito
        Optional<Carrito> carrito = carritoService.traerCarritoPorIdUsuario(id_usuario);
        if (carrito.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Carrito no encontrado");
        }

        return ResponseEntity.ok(carrito.get());
    }
    
}