package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.controller;

import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto.registrarDTO.PerfilUsuarioCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto.PerfilUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto.PerfilUsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.service.IPerfilUsuarioService;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.service.UsuarioServiceImpl;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfilUsuario")
public class PerfilUsuarioController {

    private final IPerfilUsuarioService perfilService;
    private final UsuarioServiceImpl usuarioService;

    public PerfilUsuarioController(IPerfilUsuarioService perfilService, UsuarioServiceImpl usuarioService) {
        this.perfilService = perfilService;
        this.usuarioService = usuarioService;   
    }

    @PostMapping("/private/crear/admin")
    public ResponseEntity<?> crearOActualizarParaAdmin(@RequestBody @Valid PerfilUsuarioDTO dto) {
        Usuario usuario = usuarioService.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getUsuarioId()));
        PerfilUsuarioCreateDTO createDto = mapToCreateDTO(dto);
        PerfilUsuarioResponseDTO resp = perfilService.crearOActualizarPerfil(usuario, createDto);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/public/crear/usuario")
    public ResponseEntity<?> crearOActualizarParaUsuario(@RequestBody @Valid PerfilUsuarioDTO dto) {
        Usuario usuario = usuarioService.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getUsuarioId()));
        PerfilUsuarioCreateDTO createDto = mapToCreateDTO(dto);
        PerfilUsuarioResponseDTO resp = perfilService.crearOActualizarPerfil(usuario, createDto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/private")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> obtenerPropio(Authentication auth) {
        Usuario usuario = ((UserAuthDetails) auth.getPrincipal()).getUsuario();
        Optional<PerfilUsuarioResponseDTO> opt = perfilService.obtenerPorUsuario(usuario);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of("message", "Perfil no encontrado")));
    }

    @GetMapping("/public/{usuarioId}")
    public ResponseEntity<?> obtenerPublico(@PathVariable Long usuarioId) {
        Optional<PerfilUsuarioResponseDTO> opt = perfilService.obtenerPorUsuarioId(usuarioId);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of("message", "Perfil no encontrado")));
    }

    // Helper: mapear PerfilUsuarioDTO -> PerfilUsuarioCreateDTO
    private PerfilUsuarioCreateDTO mapToCreateDTO(PerfilUsuarioDTO dto) {
        PerfilUsuarioCreateDTO c = new PerfilUsuarioCreateDTO();
        c.setNombre(dto.getNombre());
        c.setApellido(dto.getApellido());
        c.setDocumento(dto.getDocumento());
        c.setFechaNacimiento(dto.getFechaNacimiento());
        c.setGenero(dto.getGenero());
        c.setCargo(dto.getCargo());
        c.setCorreoAlternativo(dto.getCorreoAlternativo());
        return c;
    }
}
