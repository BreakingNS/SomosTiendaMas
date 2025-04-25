package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.RefreshTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public RefreshTokenService(IRefreshTokenRepository refreshTokenRepository, 
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
    
    public Map<String, String> refrescarTokens(String requestRefreshToken, HttpServletRequest request) {
        // 1. Extraer access token del header
        String tokenAnterior = extraerAccessTokenDesdeHeader(request);
        System.out.println("1. Token recibido (JWT): " + tokenAnterior);

        // 2. Obtener usuario desde el access token (aunque esté expirado)
        String usuarioId = jwtTokenProvider.obtenerUsernameDelToken(tokenAnterior); // asumimos que devuelve ID como String
        System.out.println("2. Usuario ID extraído del token: " + usuarioId);
        Usuario usuario = usuarioRepository.findById(Long.parseLong(usuarioId))
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        
        // 3. Validar y revocar el refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
            .map(this::verificarExpiracion)
            .orElseThrow(() -> new RuntimeException("Refresh token no válido."));
        System.out.println("3. Refresh recibido: " + requestRefreshToken);
        System.out.println("4. Usuario encontrado: " + usuario.getUsername());
        
        refreshToken.setUsado(true);
        refreshToken.setRevocado(true);
        refreshToken.setFechaRevocado(Instant.now());
        refreshTokenRepository.save(refreshToken);

        // 4. Revocar el access token usado en esta sesión
        tokenEmitidoService.revocarToken(tokenAnterior);

        // 5. Revocar la sesión activa correspondiente al access token
        sesionActivaService.revocarSesion(tokenAnterior);

        // 6. Crear nuevos tokens
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        String nuevoAccessToken = jwtTokenProvider.generarTokenConUsuario(usuario); // genera nuevo JWT
        String nuevoRefreshToken = crearRefreshToken(usuario.getIdUsuario(), request).getToken();

        // 7. Registrar nueva sesión
        sesionActivaService.registrarSesion(nuevoAccessToken, usuario, ip, userAgent);

        // 8. Devolver ambos tokens
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", nuevoAccessToken);
        response.put("refreshToken", nuevoRefreshToken);
        return response;
    }
    
    /*
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

        // 3. Revocar tokens activos del usuario
        tokenEmitidoService.revocarTokensActivosPorUsuario(usuario.getIdUsuario());
        
        // 4. Devolver ambos
        return Map.of(
            "accessToken", nuevoJwt,
            "refreshToken", nuevoRefreshToken.getToken()
        );
    }
    */
    
    private String extraerAccessTokenDesdeHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de acceso no encontrado o mal formado.");
        }
        return authHeader.substring(7); // Quita "Bearer "
    }
}
