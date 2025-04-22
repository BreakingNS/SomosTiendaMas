package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenBlacklist;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenBlacklistRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ITokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioRepository usuarioRepository;

    public void revocarToken(String jwt) {
        if (!jwtTokenProvider.validarToken(jwt)) {
            throw new RuntimeException("Token inválido o expirado.");
        }

        Instant fechaExpiracion = jwtTokenProvider.obtenerFechaExpiracion(jwt);
        String username = jwtTokenProvider.obtenerUsernameDelToken(jwt);
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        TokenBlacklist blacklistEntry = new TokenBlacklist();
        blacklistEntry.setToken(jwt);
        blacklistEntry.setFechaExpiracion(fechaExpiracion);
        blacklistEntry.setRevocado(true);
        blacklistEntry.setUsuario(usuario);

        tokenBlacklistRepository.save(blacklistEntry);
    }
    
    /*
    public void revocarToken(String jwt) {
        if (tokenBlacklistRepository.existsByToken(jwt)) {
            return; // Ya está revocado
        }

        String username = jwtTokenProvider.obtenerUsernameDelToken(jwt);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado para el token.");
        }

        Instant expiracion = jwtTokenProvider.obtenerFechaExpiracion(jwt);

        TokenBlacklist blacklist = new TokenBlacklist();
        blacklist.setToken(jwt);
        blacklist.setFechaExpiracion(expiracion);
        blacklist.setRevocado(true);
        blacklist.setUsuario(usuarioOpt.get());

        tokenBlacklistRepository.save(blacklist);
    }
    */
    
}
