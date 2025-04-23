package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.AuthResponse;
import com.breakingns.SomosTiendaMas.auth.dto.LoginRequest;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import static java.time.Instant.now;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/*
@Service
public class AuthService {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private IUsuarioRepository usuarioRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private ISesionActivaRepository sesionActivaRepository;
    
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

        String refreshToken = refreshTokenService.crearRefreshToken(usuario.getId_usuario(), request).getToken();

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtTokenProvider.getJwtExpirationMs());
        
        SesionActiva sesion = new SesionActiva();
        sesion.setToken(accessToken);
        sesion.setIp(ip);
        sesion.setUserAgent(userAgent);
        sesion.setUsuario(usuario);
        sesion.setFechaInicioSesion(now.toInstant());
        sesion.setFechaExpiracion(expiryDate.toInstant()); // opcional
        sesionActivaRepository.save(sesion);
        
        return new AuthResponse(accessToken, refreshToken);
    }
}
*/

@Service
public class AuthService {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ISesionActivaRepository sesionActivaRepository;

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
}