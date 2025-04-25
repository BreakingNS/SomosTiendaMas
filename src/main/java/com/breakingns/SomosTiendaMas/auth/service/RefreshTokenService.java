package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.RefreshTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt-refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(IRefreshTokenRepository refreshTokenRepository, 
                                IUsuarioRepository usuarioRepository,
                                JwtTokenProvider jwtTokenProvider
                                ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public RefreshToken crearRefreshToken(Long userId, HttpServletRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));
        
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setFechaExpiracion(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setIp(ip);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setRevocado(false);
        refreshToken.setUsado(false);

        return refreshTokenRepository.save(refreshToken);
    }
    
    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RefreshTokenException("Token no encontrado."));

        if (refreshToken.getRevocado() || refreshToken.getUsado()) {
            throw new RefreshTokenException("Token ya fue revocado o usado.");
        }

        refreshToken.setRevocado(true);
        refreshToken.setUsado(false);
        refreshToken.setFechaRevocado(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }
    
    public void logoutTotal(String username) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        Instant ahora = Instant.now();
        tokens.forEach(token -> {
            token.setRevocado(true);
            token.setFechaRevocado(ahora);
        });
        refreshTokenRepository.saveAll(tokens);
    }
    
    public Optional<RefreshToken> encontrarPorToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    public Optional<RefreshToken> verificarValidez(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.getRevocado() && !rt.getUsado())
                .filter(rt -> rt.getFechaExpiracion().isAfter(Instant.now()));
    }
        
    public RefreshToken verificarExpiracion(RefreshToken token) {
        if (token.getFechaExpiracion().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException("El refresh token ha expirado. Por favor, inicie sesión nuevamente.");
        }

        if (token.getRevocado() || token.getUsado()) {
            throw new RefreshTokenException("El refresh token ya fue usado o revocado.");
        }

        return token;
    }

    public RefreshToken guardar(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }
    
    public void borrarTokensDeUsuario(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));
        refreshTokenRepository.deleteByUsuario(usuario);
    }
    
    public Map<String, String> refrescarTokens(String requestToken, HttpServletRequest request) {
        RefreshToken refreshToken = encontrarPorToken(requestToken)
                .map(this::verificarExpiracion)
                .orElseThrow(() -> new RuntimeException("Refresh token no válido."));

        // 1. Marcar como usado y revocado
        refreshToken.setUsado(true);
        refreshToken.setRevocado(true);
        refreshToken.setFechaRevocado(Instant.now());
        guardar(refreshToken);

        // 2. Generar nuevo JWT y refreshToken
        Usuario usuario = refreshToken.getUsuario();
        String nuevoJwt = jwtTokenProvider.generarTokenDesdeUsername(usuario.getUsername());
        RefreshToken nuevoRefreshToken = crearRefreshToken(usuario.getIdUsuario(), request);

        // 3. Devolver ambos
        return Map.of(
            "accessToken", nuevoJwt,
            "refreshToken", nuevoRefreshToken.getToken()
        );
    }
}
