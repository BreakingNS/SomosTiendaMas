package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.shared.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegistroUsuarioDTO registroDTO,
                                               HttpServletRequest request) {
        String ip = RequestUtil.obtenerIpCliente(request);
        usuarioService.registrarConRolDesdeDTO(registroDTO, ip);
        return ResponseEntity.ok("Usuario registrado correctamente.");
    }
    
}
