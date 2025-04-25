package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaResponse;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final SesionActivaService sesionActivaService;
    private final UsuarioService usuarioService;

    @Autowired
    public AuthController(AuthService authService, 
                            RefreshTokenService refreshTokenService, 
                            SesionActivaService sesionActivaService,
                            UsuarioService usuarioService
                            ) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.sesionActivaService = sesionActivaService;
        this.usuarioService = usuarioService;
    }
    
    // ---
    
    @PostMapping("/login") //SE USA
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse tokens = authService.login(loginRequest, request);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/logout") //SE USA
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request,
                                    @RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");

        authService.logout(accessToken, request.getRefreshToken());

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
        
    @PostMapping("/logout-total") //SE USA
    public ResponseEntity<?> logoutTotal(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // "Bearer ..."
        authService.logoutTotal(token);
        return ResponseEntity.ok(Map.of("message", "Sesiones cerradas en todos los dispositivos"));
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refrescarToken(@RequestBody RefreshTokenRequest refresh, HttpServletRequest request) {
        Map<String, String> nuevosTokens = refreshTokenService.refrescarTokens(refresh.getRefreshToken(), request);
        return ResponseEntity.ok(nuevosTokens);
    }
    
    @GetMapping("/sesiones")
    public ResponseEntity<List<SesionActivaResponse>> sesionesActivas() {
        UserAuthDetails userDetails = (UserAuthDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long idUsuario = userDetails.getId(); // si lo necesitas
        String username = userDetails.getUsername();

        Usuario usuario = usuarioService.findByUsername(username);
        List<SesionActivaResponse> sesiones = sesionActivaService.obtenerSesionesActivas(usuario);
        return ResponseEntity.ok(sesiones);
    }
    
    @DeleteMapping("/logout/{idSesion}")
    public ResponseEntity<?> cerrarSesion(@PathVariable Long idSesion) {
        sesionActivaService.cerrarSesion(idSesion);
        return ResponseEntity.ok("Sesión cerrada con éxito");
    }

    @PostMapping("/logout-otras-sesiones")
    public ResponseEntity<?> logoutOtrasSesiones(@RequestHeader("Authorization") String authHeader) {
        String tokenActual = authHeader.replace("Bearer ", "");
        sesionActivaService.cerrarOtrasSesiones(tokenActual);
        return ResponseEntity.ok("Sesiones cerradas excepto la actual");
    }
    
    /*
    PARA UTILIZAR COOKIESSSSSSSSSSSSSSSSS -------------------------------
    @PostMapping("/loginNew")
    public ResponseEntity<AuthResponse> login2(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse tokens = authService.login(loginRequest, request);

        // Preparar cookie con refresh token
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false) // true si usás HTTPS
                .path("/api/refresh-token")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        // Devolver access token en body, refresh token en cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new AuthResponse(tokens.getAccessToken(), null)); // no mandamos el refresh en body
    }
    */

    /*
    PARA COOKIESSSSSSSSSSSSSSSSSSS ---------------------------
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refrescarTokenDesdeCookie(@CookieValue(name = "refreshToken", required = false) String requestToken,
                                                       HttpServletRequest request) {
        if (requestToken == null || requestToken.isBlank()) {
            throw new RefreshTokenException("No se encontró el refresh token en la cookie.");
        }

        return refreshTokenService.encontrarPorToken(requestToken)
                .map(refreshTokenService::verificarExpiracion)
                .map(refreshToken -> {
                    if (refreshToken.getRevocado() || refreshToken.getUsado()) {
                        throw new RefreshTokenException("Refresh token inválido o ya utilizado.");
                    }

                    // Marcar como revocado y usado
                    refreshToken.setUsado(true);
                    refreshToken.setRevocado(true);
                    refreshToken.setFechaRevocado(Instant.now());
                    refreshTokenService.guardar(refreshToken);

                    // Generar nuevos tokens
                    Usuario usuario = refreshToken.getUsuario();
                    String nuevoJwt = jwtTokenProvider.generarTokenDesdeUsername(usuario.getUsername());
                    RefreshToken nuevoRefreshToken = refreshTokenService.crearRefreshToken(usuario.getId_usuario(), request);

                    // Nueva cookie con el nuevo refresh token
                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", nuevoRefreshToken.getToken())
                            .httpOnly(true)
                            .secure(false) // true si usás HTTPS
                            .path("/api/refresh-token")
                            .maxAge(Duration.ofDays(7))
                            .sameSite("Strict")
                            .build();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .body(Map.of("accessToken", nuevoJwt));
                })
                .orElseThrow(() -> new RefreshTokenException("Refresh token no válido."));
    }
    */
}

