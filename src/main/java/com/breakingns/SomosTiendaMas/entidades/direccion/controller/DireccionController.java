package com.breakingns.SomosTiendaMas.entidades.direccion.controller;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.service.IDireccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/direccion")
public class DireccionController {

    @Autowired
    private IDireccionService direccionService;

    @PostMapping("/public")
    public ResponseEntity<DireccionResponseDTO> registrarDireccion(@RequestBody RegistroDireccionDTO dto) {
        DireccionResponseDTO response = direccionService.registrarDireccion(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/private/{id}")
    public ResponseEntity<DireccionResponseDTO> actualizarDireccion(@PathVariable Long id, @RequestBody ActualizarDireccionDTO dto) {
        DireccionResponseDTO response = direccionService.actualizarDireccion(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<DireccionResponseDTO> obtenerDireccion(@PathVariable Long id) {
        DireccionResponseDTO response = direccionService.obtenerDireccion(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/usuario/{idUsuario}")
    public ResponseEntity<List<DireccionResponseDTO>> listarDireccionesPorUsuario(@PathVariable Long idUsuario) {
        List<DireccionResponseDTO> response = direccionService.listarDireccionesPorUsuario(idUsuario);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/empresa/{idPerfilEmpresa}")
    public ResponseEntity<List<DireccionResponseDTO>> listarDireccionesPorPerfilEmpresa(@PathVariable Long idPerfilEmpresa) {
        List<DireccionResponseDTO> response = direccionService.listarDireccionesPorPerfilEmpresa(idPerfilEmpresa);
        return ResponseEntity.ok(response);
    }

    // Puedes agregar más endpoints según la lógica de negocio
}
