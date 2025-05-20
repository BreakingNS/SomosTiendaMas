package com.breakingns.SomosTiendaMas.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.utils.HeaderUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }
    
    @PostMapping("/public/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse tokens = authService.login(loginRequest, request);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/public/refresh-token")
    public ResponseEntity<AuthResponse> refrescarToken(@RequestBody @Valid RefreshTokenRequest refresh, HttpServletRequest request) {
        AuthResponse response = refreshTokenService.refrescarTokens(refresh.refreshToken(), request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/private/logout")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logout(@RequestBody @Valid RefreshTokenRequest request,
                                    @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = HeaderUtils.extraerAccessToken(authorizationHeader);
        authService.logout(accessToken, request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Sesion cerrada correctamente"));
    }
    
    @PostMapping("/private/logout-total")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutTotal(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = HeaderUtils.extraerAccessToken(authorizationHeader);
        authService.logoutTotal(accessToken);
        return ResponseEntity.ok(Map.of("message", "Sesiones cerradas en todos los dispositivos"));
    }
    
}
