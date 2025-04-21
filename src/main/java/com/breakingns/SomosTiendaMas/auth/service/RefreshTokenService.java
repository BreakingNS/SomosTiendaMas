package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt-refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;

    public RefreshTokenService(IRefreshTokenRepository refreshTokenRepository, IUsuarioRepository usuarioRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public RefreshToken crearRefreshToken(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setFechaExpiracion(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRevocado(false);
        refreshToken.setUsado(false);

        return refreshTokenRepository.save(refreshToken);
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
            throw new RuntimeException("El refresh token expiró. Por favor, logueate de nuevo.");
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
}
