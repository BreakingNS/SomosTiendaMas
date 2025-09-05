package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.request.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.dto.response.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.repository.IEventoAuditoriaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.util.RevocacionUtils;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.helpers.TokenHelper;
import com.breakingns.SomosTiendaMas.security.exception.CredencialesInvalidasException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoEncontradaException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoValidaException;
import com.breakingns.SomosTiendaMas.security.exception.TokenNoEncontradoException;
import com.breakingns.SomosTiendaMas.security.exception.TokenRevocadoException;
import com.breakingns.SomosTiendaMas.security.exception.TooManyRequestsException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioBloqueadoException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioDesactivadoException;
import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.utils.UsuarioUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final EmailVerificacionService emailVerificacionService;
    private final EventoAuditoriaService eventoAuditoriaService;

    private final IUsuarioRepository usuarioRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IEventoAuditoriaRepository eventoAuditoriaRepository;

    private final UsuarioUtils usuarioUtils;
    private final RevocacionUtils revocacionUtils;

    public AuthService(JwtTokenProvider jwtTokenProvider, 
                        AuthenticationManager authenticationManager, 
                        RefreshTokenService refreshTokenService, 
                        TokenEmitidoService tokenEmitidoService, 
                        SesionActivaService sesionActivaService, 
                        PasswordResetService passwordResetService, 
                        LoginAttemptService loginAttemptService,
                        EmailVerificacionService emailVerificacionService,
                        EventoAuditoriaService eventoAuditoriaService,
                        IUsuarioRepository usuarioRepository,
                        ITokenEmitidoRepository tokenEmitidoRepository,
                        IEventoAuditoriaRepository eventoAuditoriaRepository,
                        UsuarioUtils usuarioUtils,
                        RevocacionUtils revocacionUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
        this.passwordResetService = passwordResetService;
        this.loginAttemptService = loginAttemptService;
        this.emailVerificacionService = emailVerificacionService;
        this.eventoAuditoriaService = eventoAuditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.eventoAuditoriaRepository = eventoAuditoriaRepository;
        this.usuarioUtils = usuarioUtils;
        this.revocacionUtils = revocacionUtils;
    }
    
    // ---
    
    @Transactional
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        System.out.println("\n\n[DEBUG] Entrando al método AuthService.login para usuario: " + loginRequest.username() + "\n\n");

        if (loginAttemptService.isBlocked(loginRequest.username(), RequestUtil.obtenerIpCliente(request))) {
            System.out.println("\n\n[DEBUG] Usuario bloqueado en login: " + loginRequest.username() + "\n\n");
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

            // Verifica si el email está verificado
            if (!usuario.getEmailVerificado()) {
                System.out.println("\n\n[DEBUG] Email no verificado en login: " + loginRequest.username() + "\n\n");
                throw new CredencialesInvalidasException("El email no está verificado.");
            }

            if (!usuario.isActivo()) {
                System.out.println("\n\n[DEBUG] Usuario desactivado en login: " + loginRequest.username() + "\n\n");
                throw new UsuarioDesactivadoException("El usuario está desactivado.");
            }

            String refreshToken = refreshTokenService.crearRefreshToken(usuario.getIdUsuario(), request).getToken();

            String ip = RequestUtil.obtenerIpCliente(request);
            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null) {
                userAgent = "Desconocido";
            }

            sesionActivaService.registrarSesion(usuario, accessToken, refreshToken, ip, userAgent);

            loginAttemptService.loginSucceeded(loginRequest.username(), ip);

            eventoAuditoriaService.registrarEvento(
                loginRequest.username(),
                "LOGIN_EXITOSO",
                "Login exitoso desde IP " + RequestUtil.obtenerIpCliente(request)
            );

            System.out.println("\n\n[DEBUG] Login exitoso en AuthService.login para usuario: " + loginRequest.username() + "\n\n");

            return new AuthResponse(accessToken, refreshToken);
        }catch (AuthenticationException ex) {
            String ip = RequestUtil.obtenerIpCliente(request);

            // Registrar evento de auditoría para login fallido
            eventoAuditoriaService.registrarEvento(
                loginRequest.username(),
                "LOGIN_FALLIDO",
                "Login fallido desde IP " + ip
            );

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

    // Devuelve los datos del usuario autenticado usando el request (token JWT)
    public Map<String, Object> traerDatosUsuarioAutenticado(HttpServletRequest request) {
        // Ejemplo: obtén el username del token y busca el usuario en la BD
        String username = TokenHelper.getUsernameFromRequest(request);
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        Map<String, Object> datos = new HashMap<>();
        datos.put("id", usuario.getIdUsuario());
        datos.put("username", usuario.getUsername());
        datos.put("email", usuario.getEmail());
        // Agrega más campos si lo necesitas
        return datos;
    }

    // Verifica el código de email (simulado, deberías implementar la lógica real)
    public boolean verificarCodigoEmail(String code) {
        return emailVerificacionService.verificarCodigo(code);
    }

    public boolean seGeneroEventoAuditoria(String username, String tipoEvento) {
        // Supongamos que tienes un repositorio eventoAuditoriaRepository
        // y una entidad EventoAuditoria con campos username y tipoEvento

        return eventoAuditoriaRepository.existsByUsernameAndTipoEvento(username, tipoEvento);
    }

}