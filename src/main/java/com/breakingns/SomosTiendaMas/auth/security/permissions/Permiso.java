package com.breakingns.SomosTiendaMas.auth.security.permissions;

import com.breakingns.SomosTiendaMas.auth.dto.request.RefreshTokenRequest;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.TokenEmitidoService;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import org.springframework.stereotype.Component;

@Component("permiso")
public class Permiso {
    
    private final JwtTokenProvider jwtTokenProvider;

    private final TokenEmitidoService tokenEmitidoService;
    
    private final IUsuarioRepository usuarioRepository;

    private final IRefreshTokenRepository refreshTokenRepository;

    public Permiso(JwtTokenProvider jwtTokenProvider, TokenEmitidoService tokenEmitidoService, IUsuarioRepository usuarioRepository, IRefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenEmitidoService = tokenEmitidoService;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    public boolean puedeCerrarSesion(RefreshTokenRequest request, String authorizationHeader) {
        try {
            String accessToken = extraerBearer(authorizationHeader);
            if (!tokenEmitidoService.estaRevocado(accessToken)) {
                System.out.println("Token no activo");
                return false;
            }

            String username = jwtTokenProvider.obtenerUsernameDelToken(accessToken);
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            if (usuario == null) {
                return false;
            }

            RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken()).orElse(null);
            if (refreshToken == null) {
                return false;
            }

            return usuario.getIdUsuario().equals(refreshToken.getUsuario().getIdUsuario());
        } catch (Exception e) {
            System.out.println("Error en validación de permiso logout: " + e.getMessage());
            return false;
        }
    }
    
    public String extraerBearer(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // El token comienza después de "Bearer " (7 caracteres)
        }
        return null; // Si no tiene el formato adecuado, devuelve null
    }
    
}
