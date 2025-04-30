package com.breakingns.SomosTiendaMas.controller.user;

import com.breakingns.SomosTiendaMas.model.Carrito;
import com.breakingns.SomosTiendaMas.security.exception.CarritoNoEncontradoException;
import com.breakingns.SomosTiendaMas.service.CarritoService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    
    private final CarritoService carritoService;

    @Autowired
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id_usuario == principal.id")
    @GetMapping("/traer/{id_usuario}")
    public ResponseEntity<?> verCarrito(@PathVariable Long id_usuario) {

        // OK, puede ver el carrito
        Optional<Carrito> carrito = carritoService.traerCarritoPorIdUsuario(id_usuario);
        if (carrito.isEmpty()) {
            throw new CarritoNoEncontradoException("Carrito no encontrado");
        }
        
        return ResponseEntity.ok(carrito.get());
    }
    
}