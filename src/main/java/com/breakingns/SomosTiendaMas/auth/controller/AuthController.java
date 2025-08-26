package com.breakingns.SomosTiendaMas.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.helpers.TokenHelper;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.utils.CookieUtils;
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
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest loginRequest, 
            HttpServletRequest request,
            HttpServletResponse response) {
        
        AuthResponse tokens = authService.login(loginRequest, request);
        
        // Usar utilidad para setear cookies
        CookieUtils.setAuthCookies(response, tokens.getAccessToken(), tokens.getRefreshToken(), false); //EN PRODUCCION SERA TRUE EL SECURE.
        
        // Devolver solo mensaje de éxito
        return ResponseEntity.ok(Map.of(
            "message", "Login exitoso",
            "usuario", loginRequest.username()
        ));
    }

    @PostMapping("/public/refresh-token")
    public ResponseEntity<Map<String, String>> refrescarToken(
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        String refreshToken = TokenHelper.extractRefreshToken(request, body);
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token no encontrado"));
        }

        AuthResponse tokens = refreshTokenService.refrescarTokens(refreshToken, request);
        CookieUtils.setAuthCookies(response, tokens.getAccessToken(), tokens.getRefreshToken(), false);

        return ResponseEntity.ok(Map.of(
            "message", "Tokens renovados exitosamente"
        ));
    }

    @PostMapping("/private/logout")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Leer access token desde las cookies o header
        String accessToken = TokenHelper.extractAccessToken(request, null);

        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Access token no encontrado"));
        }

        // El service se encarga de buscar la sesión y revocar ambos tokens
        authService.logout(accessToken);

        CookieUtils.clearAuthCookies(response, false);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    @PostMapping("/private/logout-total")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutTotal(HttpServletRequest request, HttpServletResponse response) {
        
        // Leer access token desde las cookies o header
        String accessToken = TokenHelper.extractAccessToken(request, null);
        
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Access token no encontrado"));
        }
        
        // Llamar al service para hacer logout total
        authService.logoutTotal(accessToken);
        
        // Limpiar las cookies
        CookieUtils.clearAuthCookies(response, false); //EN PRODUCCION SERA TRUE EL SECURE.

        return ResponseEntity.ok(Map.of("message", "Sesiones cerradas en todos los dispositivos"));
    }

}
