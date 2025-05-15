package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.shared.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.auth.utils.TokenUtils;
import com.breakingns.SomosTiendaMas.security.exception.TokenInvalidoException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sesiones")
public class SesionController {
    
    private final AuthService authService;

    private final SesionActivaService sesionActivaService;
    private final TokenEmitidoService tokenEmitidoService;
    
    private final JwtTokenProvider jwtTokenProvider;

    public SesionController(AuthService authService, SesionActivaService sesionActivaService, TokenEmitidoService tokenEmitidoService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.sesionActivaService = sesionActivaService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @GetMapping("/private/activas")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public List<SesionActivaDTO> misSesionesActivas() {
        Long idUsuario = tokenEmitidoService.obtenerIdDesdeToken();
        return sesionActivaService.listarSesionesActivas(idUsuario);
    }

    @GetMapping("/private/admin/activas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<SesionActivaDTO> listarSesionesActivas(@RequestParam(required = false) Long idUsuario) {
        return sesionActivaService.listarSesionesActivasComoAdmin(idUsuario);
    }
    
    @PostMapping("/private/logout-otras-sesiones")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutOtrasSesiones(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestHeader("Refresh-Token") String refreshTokenHeader) {
        
        String accessToken = TokenUtils.extractTokenFromHeader(authorizationHeader);
        if (accessToken == null) {
            throw new TokenInvalidoException("Token faltante o mal formado");
        }

        if (!jwtTokenProvider.validarToken(accessToken)) {
            throw new TokenInvalidoException("Token inválido o expirado");
        }

        Long idUsuario = tokenEmitidoService.obtenerIdDesdeToken();

        // Verificar que el ID extraído del token corresponda al accessToken actual
        if (!jwtTokenProvider.validarTokenPorId(accessToken, idUsuario)) {
            throw new TokenInvalidoException("El token no corresponde a la sesión actual.");
        }
        
        authService.logoutTotalExceptoSesionActual(idUsuario, accessToken, refreshTokenHeader);
        return ResponseEntity.ok("Sesiones cerradas excepto la actual");
    }
    
}
