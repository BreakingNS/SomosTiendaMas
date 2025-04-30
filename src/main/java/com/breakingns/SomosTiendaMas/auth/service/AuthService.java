package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.utils.RequestUtil;
import com.breakingns.SomosTiendaMas.auth.utils.UsuarioUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    private final RefreshTokenService refreshTokenService;
    private final TokenEmitidoService tokenEmitidoService;
    private final SesionActivaService sesionActivaService;
    private final PasswordResetService passwordResetService;
    
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;

    private final UsuarioUtils UsuarioUtils;

    public AuthService(JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, TokenEmitidoService tokenEmitidoService, SesionActivaService sesionActivaService, PasswordResetService passwordResetService, IPasswordResetTokenRepository passwordResetTokenRepository, IUsuarioRepository usuarioRepository, ISesionActivaRepository sesionActivaRepository, UsuarioUtils UsuarioUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
        this.passwordResetService = passwordResetService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.sesionActivaRepository = sesionActivaRepository;
        this.UsuarioUtils = UsuarioUtils;
    }
    
    // ---
    
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
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

        return new AuthResponse(accessToken, refreshToken);
    }
    
    public void logout(String accessToken, String refreshToken) {
        
        refreshTokenService.logout(refreshToken); // Revocar refresh token
        tokenEmitidoService.revocarToken(accessToken); // Revocar access token
        sesionActivaService.revocarSesion(accessToken); // Revocar sesion
    }
    
    public void logoutTotal(String accessToken) {
        String username = jwtTokenProvider.obtenerUsernameDelToken(accessToken);

        refreshTokenService.logoutTotal(username); // Revoca todos los refresh tokens
        tokenEmitidoService.revocarTodosLosTokensActivos(username); // Revoca access tokens
        sesionActivaService.revocarTodasLasSesiones(username); // Revoca sesiones
    }
    
    public void logoutTotalExceptoSesionActual(Long idUsuario, String accessToken, String refresh){
        // Revocar tolos los refresh token de este usuario
        refreshTokenService.logoutTotalExceptoSesionActual(idUsuario, refresh); //Listo
        
        // Revocar todos los access token de este usuario
        tokenEmitidoService.revocarTodosLosTokensActivosExceptoSesionActual(idUsuario, accessToken); //Listo

        // También podés revocar sesiones activas si corresponde
        sesionActivaService.revocarTodasLasSesionesExceptoSesionActual(idUsuario, accessToken); //Listo
    }
    
    public void solicitarRecuperacionPassword(String email) {
        passwordResetService.solicitarRecuperacionPassword(email);
        // Siempre devolver OK aunque no exista (por seguridad).
        
        /*
            Sugerencia mínima (no urgente):
            Podrías extraer la lógica del token a un PasswordResetService o 
            TokenResetService si querés dejar el AuthService más limpio, pero 
            no es necesario ahora.
        */
    }
}