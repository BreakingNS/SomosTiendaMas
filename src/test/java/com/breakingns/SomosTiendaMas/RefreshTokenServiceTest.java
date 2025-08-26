package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.service.SesionActivaService;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.auth.service.util.RevocacionUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.RefreshTokenException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenServiceTest {
    private final Long refreshTokenDurationMs = 5000L; // 5 segundos

    private final JwtTokenProviderTest jwtTokenProviderTest;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final SesionActivaService sesionActivaService;

    private final RevocacionUtils revocacionUtils;

    public RefreshTokenServiceTest(
        IRefreshTokenRepository refreshTokenRepository,
        IUsuarioRepository usuarioRepository,
        JwtTokenProviderTest jwtTokenProviderTest,
        TokenEmitidoService tokenEmitidoService,
        SesionActivaService sesionActivaService,
        RevocacionUtils revocacionUtils
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProviderTest = jwtTokenProviderTest;
        this.sesionActivaService = sesionActivaService;
        this.revocacionUtils = revocacionUtils; 
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

        // 1. Buscar y revocar el refresh token anterior
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
            .map(this::verificarExpiracion)
            .orElseThrow(() -> new RefreshTokenException("Refresh token no válido."));
        revocarYUsar(refreshToken);

        // 2. Buscar y revocar la sesión activa asociada al refresh token
        revocacionUtils.revocarTodoPorSesion(sesionActivaService.buscarPorRefreshToken(refreshToken.getToken()));

        // 3. Obtener el usuario
        Usuario usuario = refreshToken.getUsuario();

        // 4. Generar nuevos tokens y registrar nueva sesión
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

    private AuthResponse generarYRegistrarTokens(Usuario usuario, HttpServletRequest request) {
        String nuevoAccessToken = jwtTokenProviderTest.generarTokenConUsuario(usuario);
        String nuevoRefreshToken = crearRefreshToken(usuario.getIdUsuario(), request).getToken();
        sesionActivaService.registrarSesion(
            usuario,
            nuevoAccessToken,
            nuevoRefreshToken,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        return new AuthResponse(nuevoAccessToken, nuevoRefreshToken);
    }
    /* 
    private String extraerAccessTokenDesdeHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenNoEncontradoException("Token de acceso no encontrado o mal formado.");
        }
        return authHeader.substring(7);
    }*/
}
