package com.breakingns.SomosTiendaMas.auth.controller;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.JwtResponse;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.AuthService;
import com.breakingns.SomosTiendaMas.auth.service.RefreshTokenService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    /*
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) { //recibe en el cuerpo del request (JSON) un objeto LoginRequest.
        //Autenticacion
        Authentication authentication = authenticationManager.authenticate( //intenta autenticar el usuario usando el Authenticacion Manager.
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        //Guardar la autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication); //Esto guarda la autenticación en el contexto de seguridad de Spring para que esté disponible mientras dure la sesión de esa request
        
        //Generación del Token JWT
        String token = jwtTokenProvider.generarToken(authentication); //Llama a un método que genera un JWT (JSON Web Token) a partir del usuario autenticado.
        
        //Respuesta
        return ResponseEntity.ok(new JwtResponse(token)); //Devuelve una respuesta HTTP 200 con un objeto JwtResponse que contiene el token generado.
    }
    
    @PostMapping("/loginNew")
    public ResponseEntity<AuthResponse> login2(@RequestBody LoginRequest loginRequest) {
        AuthResponse tokens = authService.login(loginRequest);
        return ResponseEntity.ok(tokens);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
    
    /*
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refrescarToken(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.encontrarPorToken(requestToken)
                .map(refreshTokenService::verificarExpiracion)
                .map(RefreshToken::getUsuario)
                .map(usuario -> {
                    String nuevoJwt = jwtTokenProvider.generarTokenDesdeUsername(usuario.getUsername());
                    return ResponseEntity.ok(Map.of(
                        "accessToken", nuevoJwt,
                        "refreshToken", requestToken // o generar uno nuevo si querés rotar
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no válido."));
    }
    */
    /*
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.verificarValidez(requestToken)
                .map(RefreshToken::getUsuario)
                .map(usuario -> {
                    String newAccessToken = jwtTokenProvider.generarToken(
                            new UsernamePasswordAuthenticationToken(
                                    usuario.getUsername(), null, usuario.getRoles()
                            )
                    );
                    return ResponseEntity.ok(new AuthResponse(newAccessToken, requestToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no válido o expirado"));
    }
    */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refrescarToken(@RequestBody RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        return refreshTokenService.encontrarPorToken(requestToken)
                .map(refreshTokenService::verificarExpiracion)
                .map(refreshToken -> {
                    // 1. Marcar como usado y revocado
                    refreshToken.setUsado(true);
                    refreshToken.setRevocado(false);
                    refreshToken.setFechaRevocado(Instant.now());
                    refreshTokenService.guardar(refreshToken); // método que hace save()

                    // 2. Generar nuevo JWT y nuevo refreshToken
                    Usuario usuario = refreshToken.getUsuario();
                    String nuevoJwt = jwtTokenProvider.generarTokenDesdeUsername(usuario.getUsername());
                    RefreshToken nuevoRefreshToken = refreshTokenService.crearRefreshToken(usuario.getId_usuario());

                    // 3. Devolver ambos nuevos tokens
                    return ResponseEntity.ok(Map.of(
                        "accessToken", nuevoJwt,
                        "refreshToken", nuevoRefreshToken.getToken()
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no válido."));
    }
}

