package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TokenEmitidoService {

    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IUsuarioRepository usuarioRepository;

    public TokenEmitidoService(ITokenEmitidoRepository tokenEmitidoRepository, IUsuarioRepository usuarioRepository) {
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void guardarToken(String token, Instant fechaExpiracion, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        TokenEmitido nuevoToken = new TokenEmitido();
        nuevoToken.setToken(token);
        nuevoToken.setFechaEmision(Instant.now());
        nuevoToken.setFechaExpiracion(fechaExpiracion);
        nuevoToken.setRevocado(false);
        nuevoToken.setUsuario(usuario);

        tokenEmitidoRepository.save(nuevoToken);
    }

    public void revocarToken(String jwt) { //SE USA
        tokenEmitidoRepository.findByToken(jwt).ifPresent(tokenEmitido -> {
            tokenEmitido.setRevocado(true);
            tokenEmitidoRepository.save(tokenEmitido);
        });
    }

    public void revocarTodosLosTokensActivos(String username) {
        List<TokenEmitido> tokens = tokenEmitidoRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        tokens.forEach(t -> t.setRevocado(true));
        tokenEmitidoRepository.saveAll(tokens);
    }

    public boolean estaRevocado(String token) {
        return tokenEmitidoRepository.findByToken(token)
                .map(TokenEmitido::isRevocado)
                .orElse(true); // Si no está registrado, lo tratamos como inválido
    }
    
    public Long obtenerIdDesdeToken() {
        System.out.println("Entrando obtenerIdDesdeToken");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication actual: " + auth);
        Object principal = auth.getPrincipal();
        System.out.println("Principal obtenido: " + principal);
        if (principal instanceof UserAuthDetails userAuthDetails) {
            Long id = userAuthDetails.getId();
            return id;
        } else {
            throw new RuntimeException("Principal no es instancia de UserAuthDetails");
        }
    }
}