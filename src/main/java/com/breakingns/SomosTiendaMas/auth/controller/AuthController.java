package com.breakingns.SomosTiendaMas.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.request.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.utils.CookieUtils;
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
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest loginRequest, 
            HttpServletRequest request,
            HttpServletResponse response) {
        
        AuthResponse tokens = authService.login(loginRequest, request);
        
        // Usar utilidad para setear cookies
        CookieUtils.setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());
        
        // Devolver solo mensaje de éxito
        return ResponseEntity.ok(Map.of(
            "message", "Login exitoso",
            "usuario", loginRequest.username()
        ));
    }

    /* 
    @PostMapping("/public/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse tokens = authService.login(loginRequest, request);
        return ResponseEntity.ok(tokens);
    }*/
    
    @PostMapping("/public/refresh-token")
    public ResponseEntity<Map<String, String>> refrescarToken(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        // Leer refresh token desde las cookies
        String refreshToken = CookieUtils.getRefreshTokenFromCookies(request);
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token no encontrado"));
        }
        
        // Refrescar tokens
        AuthResponse tokens = refreshTokenService.refrescarTokens(refreshToken, request);
        
        // Setear nuevas cookies con los tokens rotados
        CookieUtils.setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());
        
        // Devolver solo mensaje de éxito
        return ResponseEntity.ok(Map.of(
            "message", "Tokens renovados exitosamente"
        ));
    }

    /*
    @PostMapping("/public/refresh-token")
    public ResponseEntity<AuthResponse> refrescarToken(@RequestBody @Valid RefreshTokenRequest refresh, HttpServletRequest request) {
        AuthResponse response = refreshTokenService.refrescarTokens(refresh.refreshToken(), request);
        return ResponseEntity.ok(response);
    }*/

    @PostMapping("/private/logout")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        
        // Leer tokens desde las cookies
        String accessToken = CookieUtils.getAccessTokenFromCookies(request);
        String refreshToken = CookieUtils.getRefreshTokenFromCookies(request);
        
        if (accessToken == null || refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Tokens no encontrados"));
        }
        
        // Llamar al service para hacer logout
        authService.logout(accessToken, refreshToken);
        
        // Limpiar las cookies
        CookieUtils.clearAuthCookies(response);
        
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    /* 
    @PostMapping("/private/logout")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logout(@RequestBody @Valid RefreshTokenRequest request,
                                    @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = HeaderUtils.extraerAccessToken(authorizationHeader);
        authService.logout(accessToken, request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Sesion cerrada correctamente"));
    }*/

    @PostMapping("/private/logout-total")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutTotal(HttpServletRequest request, HttpServletResponse response) {
        
        // Leer access token desde las cookies
        String accessToken = CookieUtils.getAccessTokenFromCookies(request);
        
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Access token no encontrado"));
        }
        
        // Llamar al service para hacer logout total
        authService.logoutTotal(accessToken);
        
        // Limpiar las cookies
        CookieUtils.clearAuthCookies(response);
        
        return ResponseEntity.ok(Map.of("message", "Sesiones cerradas en todos los dispositivos"));
    }

    /*
    @PostMapping("/private/logout-total")
    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutTotal(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = HeaderUtils.extraerAccessToken(authorizationHeader);
        authService.logoutTotal(accessToken);
        return ResponseEntity.ok(Map.of("message", "Sesiones cerradas en todos los dispositivos"));
    }*/
    
}
