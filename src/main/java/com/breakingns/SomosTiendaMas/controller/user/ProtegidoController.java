package com.breakingns.SomosTiendaMas.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtegidoController {
    
    @GetMapping("/ruta")
    public ResponseEntity<String> rutaProtegida() {
        return ResponseEntity.ok("Accediste a la ruta protegida.");
    }
    
}
