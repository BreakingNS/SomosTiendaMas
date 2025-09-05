package com.breakingns.SomosTiendaMas.entidades.empresa.controller;

import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.PerfilEmpresaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.service.IPerfilEmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil-empresa")
public class PerfilEmpresaController {

    @Autowired
    private IPerfilEmpresaService perfilEmpresaService;

    @PostMapping("/public")
    public ResponseEntity<PerfilEmpresaResponseDTO> registrarPerfilEmpresa(@RequestBody RegistroPerfilEmpresaDTO dto) {
        PerfilEmpresaResponseDTO response = perfilEmpresaService.registrarPerfilEmpresa(dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/private/{id}")
    public ResponseEntity<PerfilEmpresaResponseDTO> actualizarPerfilEmpresa(@PathVariable Long id, @RequestBody ActualizarPerfilEmpresaDTO dto) {
        PerfilEmpresaResponseDTO response = perfilEmpresaService.actualizarPerfilEmpresa(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<PerfilEmpresaResponseDTO> obtenerPerfilEmpresa(@PathVariable Long id) {
        PerfilEmpresaResponseDTO response = perfilEmpresaService.obtenerPerfilEmpresa(id);
        return ResponseEntity.ok(response);
    }

    // Puedes agregar más endpoints según la lógica de negocio
}
