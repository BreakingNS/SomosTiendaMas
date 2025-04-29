package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.NotFoundException;
import com.breakingns.SomosTiendaMas.security.exception.RefreshTokenException;
import com.breakingns.SomosTiendaMas.security.exception.TokenNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${app.jwt-refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final JwtTokenProvider jwtTokenProvider;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final TokenEmitidoService tokenEmitidoService;
    private final SesionActivaService sesionActivaService;

    public RefreshTokenService(
        IRefreshTokenRepository refreshTokenRepository,
        IUsuarioRepository usuarioRepository,
        JwtTokenProvider jwtTokenProvider,
        TokenEmitidoService tokenEmitidoService,
        ISesionActivaRepository sesionActivaRepository,
        SesionActivaService sesionActivaService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaRepository = sesionActivaRepository;
        this.sesionActivaService = sesionActivaService;
    }

    public RefreshToken crearRefreshToken(Long userId, HttpServletRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id: " + userId));

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        RefreshToken refreshToken = buildRefreshToken(usuario, ip, userAgent);
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
        revocarTokens(tokens);
    }

    public void logoutTotalExceptoSesionActual(Long idUsuario, String refresh) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUsuario_IdUsuarioAndRevocadoFalse(idUsuario);
        Instant ahora = Instant.now();
        tokens.forEach(token -> {
            if (!token.getToken().equals(refresh)) {
                token.setRevocado(true);
                token.setFechaRevocado(ahora);
            }
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
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id: " + userId));
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    public AuthResponse refrescarTokens(String requestRefreshToken, HttpServletRequest request) {
        String tokenAnterior = extraerAccessTokenDesdeHeader(request);
        Long usuarioId = jwtTokenProvider.obtenerIdDelToken(tokenAnterior);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verificarExpiracion)
                .orElseThrow(() -> new RefreshTokenException("Refresh token no válido."));

        revocarTokensAnteriores(tokenAnterior, refreshToken);
        return generarYRegistrarTokens(usuario, request);
    }

    // ======================= MÉTODOS PRIVADOS ===========================

    private RefreshToken buildRefreshToken(Usuario usuario, String ip, String userAgent) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setFechaExpiracion(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setIp(ip);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setRevocado(false);
        refreshToken.setUsado(false);
        return refreshToken;
    }

    private void revocarTokens(List<RefreshToken> tokens) {
        Instant ahora = Instant.now();
        tokens.forEach(token -> {
            token.setRevocado(true);
            token.setFechaRevocado(ahora);
        });
        refreshTokenRepository.saveAll(tokens);
    }

    private void revocarYUsar(RefreshToken refreshToken) {
        refreshToken.setRevocado(true);
        refreshToken.setUsado(true);
        refreshToken.setFechaRevocado(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    private void revocarTokensAnteriores(String accessToken, RefreshToken refreshToken) {
        revocarYUsar(refreshToken);
        tokenEmitidoService.revocarToken(accessToken);
        sesionActivaService.revocarSesion(accessToken);
    }

    private AuthResponse generarYRegistrarTokens(Usuario usuario, HttpServletRequest request) {
        String nuevoAccessToken = jwtTokenProvider.generarTokenConUsuario(usuario);
        String nuevoRefreshToken = crearRefreshToken(usuario.getIdUsuario(), request).getToken();
        sesionActivaService.registrarSesion(
            usuario,
            nuevoAccessToken,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        return new AuthResponse(nuevoAccessToken, nuevoRefreshToken);
    }

    private String extraerAccessTokenDesdeHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenNoEncontradoException("Token de acceso no encontrado o mal formado.");
        }
        return authHeader.substring(7);
    }
}
