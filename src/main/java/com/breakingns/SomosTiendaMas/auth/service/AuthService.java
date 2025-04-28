package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.auth.repository.IPasswordResetTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    private final RefreshTokenService refreshTokenService;
    private final TokenEmitidoService tokenEmitidoService;
    private final SesionActivaService sesionActivaService;
   
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;

    public AuthService(
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService,
            IUsuarioRepository usuarioRepository,
            AuthenticationManager authenticationManager,
            ISesionActivaRepository sesionActivaRepository,
            TokenEmitidoService tokenEmitidoService,
            SesionActivaService sesionActivaService,
            IPasswordResetTokenRepository passwordResetTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.sesionActivaRepository = sesionActivaRepository;
        this.tokenEmitidoService = tokenEmitidoService;
        this.sesionActivaService = sesionActivaService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }
    
    // ---
    
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtTokenProvider.generarToken(authentication);

        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String refreshToken = refreshTokenService
            .crearRefreshToken(usuario.getIdUsuario(), request)
            .getToken();

        // 🔸 Captura de IP y User-Agent
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        String userAgent = request.getHeader("User-Agent");

        // 🔸 Fecha actual y expiración (mismo tiempo que token)
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtTokenProvider.getJwtExpirationMs());

        // 🔸 Guardar sesión activa
        SesionActiva sesion = new SesionActiva();
        sesion.setToken(accessToken);
        sesion.setIp(ip);
        sesion.setUserAgent(userAgent);
        sesion.setUsuario(usuario);
        sesion.setFechaInicioSesion(now);
        sesion.setFechaExpiracion(expiryDate);
        sesion.setRevocado(false);
        sesionActivaRepository.save(sesion);

        return new AuthResponse(accessToken, refreshToken);
    }
    
    public void logout(String accessToken, String refreshToken) {
        // Revocar refresh token
        refreshTokenService.logout(refreshToken);

        // Revocar access token
        tokenEmitidoService.revocarToken(accessToken);
        
        // Revocar sesion
        sesionActivaService.revocarSesion(accessToken);
    }
    
    public void logoutTotal(String accessToken) {
        String username = jwtTokenProvider.obtenerUsernameDelToken(accessToken);
        
        // Revocar tolos los refresh token de este usuario
        refreshTokenService.logoutTotal(username);
        
        // Revocar todos los access token de este usuario
        tokenEmitidoService.revocarTodosLosTokensActivos(username);

        // También podés revocar sesiones activas si corresponde
        sesionActivaService.revocarTodasLasSesiones(username);
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
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String token = UUID.randomUUID().toString();

            TokenResetPassword tokenReset = new TokenResetPassword();
            tokenReset.setToken(token);
            tokenReset.setFechaExpiracion(Instant.now().plus(15, ChronoUnit.MINUTES)); // expira en 15 minutos
            tokenReset.setUsado(false);
            tokenReset.setUsuario(usuario);

            passwordResetTokenRepository.save(tokenReset);

            // Acá deberías enviar un email real, pero por ahora podemos hacer un print:
            System.out.println("Token para resetear contraseña (enviarlo por email en producción): " + token);
        }else{
            System.out.println("No hay usuario con ese correo registrado.");
        }
        // Siempre devolver OK aunque no exista (por seguridad).
    }
}