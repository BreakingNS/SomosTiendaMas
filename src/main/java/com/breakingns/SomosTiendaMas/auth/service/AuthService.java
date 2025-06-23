package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.auth.utils.UsuarioUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.CredencialesInvalidasException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoValidaException;
import com.breakingns.SomosTiendaMas.security.exception.TokenException;
import com.breakingns.SomosTiendaMas.security.exception.TooManyRequestsException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioBloqueadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
    private final IRefreshTokenRepository refreshTokenRepository;

    private final UsuarioUtils UsuarioUtils;

    public AuthService(JwtTokenProvider jwtTokenProvider, 
                        AuthenticationManager authenticationManager, 
                        RefreshTokenService refreshTokenService, 
                        TokenEmitidoService tokenEmitidoService, 
                        SesionActivaService sesionActivaService, 
                        PasswordResetService passwordResetService, 
                        LoginAttemptService loginAttemptService,
                        IUsuarioRepository usuarioRepository,
                        IRefreshTokenRepository refreshTokenRepository, 
                        UsuarioUtils UsuarioUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
        this.passwordResetService = passwordResetService;
        this.loginAttemptService = loginAttemptService;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.UsuarioUtils = UsuarioUtils;
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

            Usuario usuario = UsuarioUtils.findByUsername(loginRequest.username());

            String refreshToken = refreshTokenService.crearRefreshToken(usuario.getIdUsuario(), request).getToken();

            // Extraer IP y user-agent con helper
            String ip = RequestUtil.obtenerIpCliente(request);
            String userAgent = request.getHeader("User-Agent");

            // Crear sesión activa con servicio dedicado
            sesionActivaService.registrarSesion(usuario, accessToken, ip, userAgent);

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
    
    public void logout(String accessToken, String refreshToken) {
        // Extraer username desde el accessToken para identificar al usuario
        String username = jwtTokenProvider.obtenerUsernameDelToken(accessToken);

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Buscar el refreshToken en la base de datos
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new TokenException("Refresh token no encontrado"));

        // Verificar que el refreshToken pertenece al mismo usuario
        if (!token.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new TokenException("El refresh token no pertenece al usuario autenticado");
        }

        // Verificar si el refreshToken está revocado
        if (token.getRevocado()) {
            throw new TokenException("Refresh token ya fue revocado o usado.");
        }

        // Revocar el refresh token, el access token y la sesión activa
        refreshTokenService.logout(refreshToken); // Revocar refresh token
        tokenEmitidoService.revocarToken(accessToken); // Revocar access token
        sesionActivaService.revocarSesion(accessToken); // Revocar sesión 
    }
    
    public void logoutTotal(String accessToken) {
        String username = jwtTokenProvider.obtenerUsernameDelToken(accessToken);
        log.info("Logout total solicitado por usuario: {}", username);
        
        refreshTokenService.logoutTotal(username); // Revoca todos los refresh tokens
        tokenEmitidoService.revocarTodosLosTokensActivos(username); // Revoca access tokens
        sesionActivaService.revocarTodasLasSesiones(username); // Revoca sesiones
        
        log.info("Logout total completado para usuario: {}", username);
    }
    
    public void logoutTotalExceptoSesionActual(Long idUsuario, String accessToken, String refresh){
        log.info("Logout total excepto sesión actual para usuario ID: {}", idUsuario);
        
        if (!tokenEmitidoService.validarSesionActual(accessToken, idUsuario)) {
            throw new SesionNoValidaException("La sesión no es válida o ha sido manipulada.");
        }
        
        // Revocar tolos los refresh token de este usuario
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
}