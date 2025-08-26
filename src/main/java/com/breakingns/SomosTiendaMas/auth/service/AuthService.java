package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.shared.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.util.RevocacionUtils;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.auth.utils.UsuarioUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.CredencialesInvalidasException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoEncontradaException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoValidaException;
import com.breakingns.SomosTiendaMas.security.exception.TokenNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.TokenRevocadoException;
import com.breakingns.SomosTiendaMas.security.exception.TooManyRequestsException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioBloqueadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@Service
public class AuthService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    private final RefreshTokenService refreshTokenService;
    private final TokenEmitidoService tokenEmitidoService;
    private final SesionActivaService sesionActivaService;
    private final PasswordResetService passwordResetService;
    //private final RateLimiterService rateLimiterService;
    private final LoginAttemptService loginAttemptService;
    
    private final IUsuarioRepository usuarioRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;

    private final UsuarioUtils usuarioUtils;
    private final RevocacionUtils revocacionUtils;

    public AuthService(JwtTokenProvider jwtTokenProvider, 
                        AuthenticationManager authenticationManager, 
                        RefreshTokenService refreshTokenService, 
                        TokenEmitidoService tokenEmitidoService, 
                        SesionActivaService sesionActivaService, 
                        PasswordResetService passwordResetService, 
                        LoginAttemptService loginAttemptService,
                        IUsuarioRepository usuarioRepository,
                        ITokenEmitidoRepository tokenEmitidoRepository,
                        UsuarioUtils usuarioUtils,
                        RevocacionUtils revocacionUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
        this.passwordResetService = passwordResetService;
        this.loginAttemptService = loginAttemptService;
        this.usuarioRepository = usuarioRepository;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.usuarioUtils = usuarioUtils;
        this.revocacionUtils = revocacionUtils;
    }
    
    // ---
    
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Intento de login para usuario: {}", loginRequest.username());
        
        if (loginAttemptService.isBlocked(loginRequest.username(), RequestUtil.obtenerIpCliente(request))) {
            throw new UsuarioBloqueadoException("Usuario temporalmente bloqueado por múltiples intentos fallidos.");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.username(), loginRequest.password()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        
            String accessToken = jwtTokenProvider.generarTokenDesdeAuthentication(authentication);

            Usuario usuario = usuarioUtils.findByUsername(loginRequest.username());

            String refreshToken = refreshTokenService.crearRefreshToken(usuario.getIdUsuario(), request).getToken();

            // Extraer IP y user-agent con helper
            String ip = RequestUtil.obtenerIpCliente(request);
            String userAgent = request.getHeader("User-Agent");

            if (userAgent == null) {
                userAgent = "Desconocido"; //PRUEBA PARA EVITAR USERAGENT NULOS
            }

            // Crear sesión activa con servicio dedicado
            sesionActivaService.registrarSesion(usuario, accessToken, refreshToken, ip, userAgent);

            loginAttemptService.loginSucceeded(loginRequest.username(), ip);

            log.info("Login exitoso. Access token emitido para usuario: {}", usuario.getUsername());
            log.info("IP: {}, User-Agent: {}", ip, userAgent);

            return new AuthResponse(accessToken, refreshToken);
        } catch (AuthenticationException ex) {
            String ip = RequestUtil.obtenerIpCliente(request);
            loginAttemptService.loginFailed(loginRequest.username(), ip);
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }
    }
    
    @Transactional
    public void logout(String accessToken) {
        // Buscar la sesión activa por access token
        SesionActiva sesion = sesionActivaService.buscarPorToken(accessToken)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión no encontrada"));

        Long idSesion = sesion.getId();
        String username = sesion.getUsuario().getUsername();

        log.info("Logout solicitado para usuario: {} (sesión: {})", username, idSesion);

        // Validar que la sesión corresponde al usuario del token
        Long idUsuario = jwtTokenProvider.obtenerIdDesdeToken(accessToken);
        if (!tokenEmitidoService.validarSesionActual(accessToken, idUsuario)) {
            throw new SesionNoValidaException("La sesión no es válida o ha sido manipulada.");
        }

        // Revocar todo lo relacionado a la sesión
        revocacionUtils.revocarTodoPorSesion(idSesion);

        log.info("Logout exitoso para usuario: {} (sesión: {})", username, idSesion);
    }
    
    public void logoutTotal(String accessToken) {
        String username = jwtTokenProvider.obtenerUsernameDelToken(accessToken);
        log.info("Logout total solicitado por usuario: {}", username);
        
        refreshTokenService.logoutTotal(username); // Revoca todos los refresh tokens
        tokenEmitidoService.revocarTodosLosTokensActivos(username); // Revoca access tokens
        sesionActivaService.revocarTodasLasSesiones(username); // Revoca sesiones
        
        log.info("Logout total completado para usuario: {}", username);
    }
    
    public void logoutTotalExceptoSesionActual(Long idUsuario, String accessToken){
        log.info("Logout total excepto sesión actual para usuario ID: {}", idUsuario);
        
        if (!tokenEmitidoService.validarSesionActual(accessToken, idUsuario)) {
            throw new SesionNoValidaException("La sesión no es válida o ha sido manipulada.");
        }
        
        String refresh = obtenerRefreshDeSesionActual(idUsuario, accessToken);

        // Revocar todos los refresh token de este usuario
        refreshTokenService.logoutTotalExceptoSesionActual(idUsuario, refresh); //Listo
        
        // Revocar todos los access token de este usuario
        tokenEmitidoService.revocarTodosLosTokensActivosExceptoSesionActual(idUsuario, accessToken); //Listo

        // También podés revocar sesiones activas si corresponde
        sesionActivaService.revocarTodasLasSesionesExceptoSesionActual(idUsuario, accessToken); //Listo
        
        log.info("Logout parcial (excepto sesión actual) completado para usuario ID: {}", idUsuario);
    }
    
    @Transactional
    public void procesarSolicitudOlvidePassword(String email, String ip, HttpServletRequest request) {
        if (loginAttemptService.isBlocked(email, ip)) {
            throw new TooManyRequestsException("Demasiadas solicitudes, intenta más tarde.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        usuarioOpt.ifPresentOrElse(
            usuario -> procesarSiExiste(usuario, ip),
            () -> procesarSiNoExiste(ip)
        );
    }

    private void procesarSiExiste(Usuario usuario, String ip) {
        passwordResetService.solicitarRecuperacionPassword(usuario.getEmail());
        loginAttemptService.loginSucceeded(usuario.getUsername(), ip);
    }

    private void procesarSiNoExiste(String ip) {
        loginAttemptService.loginFailed(null, ip); // Bloqueamos solo por IP
    }

    public Integer numeroSesionesActivas(Long idUsuario){ // SOLO PRUEBAS, no produccion
        return sesionActivaService.numeroSesionesActivas(idUsuario);
    }

    public void generarTokenExpirado(String email) { //SOLO PRUEBAS
        passwordResetService.solicitarRecuperacionPasswordExpirado(email);
    }

    public void generarTokenUsado(String email) { //SOLO PRUEBAS
        passwordResetService.solicitarRecuperacionPasswordUsado(email);
    }

    public boolean validarAccessToken(String accessToken) {
        Optional<TokenEmitido> tokenOpt = tokenEmitidoRepository.findByToken(accessToken);
        if (tokenOpt.isEmpty()) {
            throw new TokenNoEncontradoException("Token no encontrado");
        }
        TokenEmitido tokenEmitido = tokenOpt.get();
        if (tokenEmitido.isRevocado()) {
            throw new TokenRevocadoException("El token fue revocado");
        }
        return true;
    }

    public boolean validarRefreshToken(String refreshToken) {
        return refreshTokenService.validarToken(refreshToken);
    }

    public void revocarAccessToken(String accessToken) {
        tokenEmitidoService.revocarToken(accessToken);
    }

    public void revocarRefreshToken(String refreshToken) {
        refreshTokenService.revocarToken(refreshToken);
    }

    public List<SesionActivaDTO> listarSesionesActivas(Long idUsuario) {
        return sesionActivaService.listarSesionesActivas(idUsuario);
    }

    private String obtenerRefreshDeSesionActual(Long idUsuario, String accessToken) {
        // Buscar la sesión activa por accessToken y usuario
        SesionActiva sesion = sesionActivaService.buscarSesionPorTokenYUsuario(accessToken, idUsuario)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión activa no encontrada para el usuario y token"));
        return sesion.getRefreshToken();
    }
}