package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {
    @Value("${app.jwt-refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final JwtTokenProvider jwtTokenProvider;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final TokenEmitidoService tokenEmitidoService;
    private final SesionActivaService sesionActivaService;

    public RefreshTokenService(
        IRefreshTokenRepository refreshTokenRepository,
        IUsuarioRepository usuarioRepository,
        JwtTokenProvider jwtTokenProvider,
        TokenEmitidoService tokenEmitidoService,
        SesionActivaService sesionActivaService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
    }

    public RefreshToken crearRefreshToken(Long userId, HttpServletRequest request) {
        log.info("Creando nuevo refresh token para userId: {}", userId);
        
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id: " + userId));

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        RefreshToken refreshToken = buildRefreshToken(usuario, ip, userAgent);
        return refreshTokenRepository.save(refreshToken);
    }
    
    @Transactional
    public void logout(String token) {
        log.info("Logout individual para token: {}", token);
        
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
        log.info("Logout total para usuario: {}", username);
        
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        revocarTokens(tokens);
    }

    public void logoutTotalExceptoSesionActual(Long idUsuario, String refresh) {
        log.info("Revocando todos los tokens menos el actual para userId: {}", idUsuario);
        
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

    @Transactional
    public RefreshToken verificarExpiracion(RefreshToken refreshToken) {
        Instant ahora = Instant.now();

        // Verificar si el token ha expirado
        if (refreshToken.getFechaExpiracion().isBefore(ahora)) {
            throw new RefreshTokenException("El refresh token ha expirado.");
        }

        // Verificar si el token ha sido revocado
        if (refreshToken.getRevocado()) {
            throw new RefreshTokenException("El refresh token ha sido revocado.");
        }

        return refreshToken;
    }

    public RefreshToken guardar(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    public void borrarTokensDeUsuario(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id: " + userId));
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    @Transactional
    public AuthResponse refrescarTokens(String requestRefreshToken, HttpServletRequest request) {
        log.info("Llamando a refrescarTokens con token: {}", requestRefreshToken);
    
        String tokenAnterior = extraerAccessTokenDesdeHeader(request);
        Long usuarioId = jwtTokenProvider.obtenerIdDelToken(tokenAnterior);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));

        // Verificar si el refresh token es válido (no expirado y no revocado)
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verificarExpiracion)
                .orElseThrow(() -> new RefreshTokenException("Refresh token no válido."));

        SesionActiva sesion = sesionActivaService.buscarPorToken(tokenAnterior)
                .orElseThrow(() -> new RefreshTokenException("No se encontró sesión para este token."));
        
        // Revocar tokens anteriores
        revocarTokensAnteriores(tokenAnterior, refreshToken);

        log.info("Token de refresh usado: {}", refreshToken);

        // Generar y registrar los nuevos tokens
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
