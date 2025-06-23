package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.utils.RsaKeyUtil;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.PrincipalInvalidoException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import java.security.PublicKey;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenEmitidoService {

    private final PublicKey publicKey;
    
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IUsuarioRepository usuarioRepository;
    
    // PRODUCCION
    public TokenEmitidoService(
            ITokenEmitidoRepository tokenEmitidoRepository, 
            IUsuarioRepository usuarioRepository, 
            RsaKeyUtil rsaKeyUtil,
            @Value("${jwt.public-key-path}") String publicKeyPath
    ) throws Exception {
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.publicKey = rsaKeyUtil.loadPublicKey(publicKeyPath);
    }

    /* DESARROLLO
    public TokenEmitidoService(
            ITokenEmitidoRepository tokenEmitidoRepository, 
            IUsuarioRepository usuarioRepository, 
            RsaKeyUtil rsaKeyUtil
    ) throws Exception {
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.publicKey = rsaKeyUtil.loadPublicKey("src/main/resources/keys/public.pem");
    }*/

    public void guardarToken(String token, Instant fechaExpiracion, String username) {
        log.info("Guardando token de usuario: {}", username);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        TokenEmitido nuevoToken = new TokenEmitido();
        nuevoToken.setToken(token);
        nuevoToken.setFechaEmision(Instant.now());
        nuevoToken.setFechaExpiracion(fechaExpiracion);
        nuevoToken.setRevocado(false);
        nuevoToken.setUsuario(usuario);

        tokenEmitidoRepository.save(nuevoToken);
    }

    @Transactional
    public void revocarToken(String jwt) {
        log.info("Revocando token: {}", jwt);
        tokenEmitidoRepository.findByToken(jwt).ifPresent(tokenEmitido -> {
            tokenEmitido.setRevocado(true);
            tokenEmitidoRepository.save(tokenEmitido);
        });
    }

    public void revocarTodosLosTokensActivos(String username) {
        log.info("Revocando todos los token de: {}", username);
        List<TokenEmitido> tokens = tokenEmitidoRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        tokens.forEach(t -> t.setRevocado(true));
        tokenEmitidoRepository.saveAll(tokens);
    }
    
    public void revocarTodosLosTokensActivosExceptoSesionActual(Long idUsuario, String accessToken) {
        log.info("Revocando todos los token activos de : {}", idUsuario);
        List<TokenEmitido> tokens = tokenEmitidoRepository.findAllByUsuario_IdUsuarioAndRevocadoFalse(idUsuario);
        tokens.forEach(token -> {
            if (!token.getToken().equals(accessToken)) {
                token.setRevocado(true);
            }
        });
        tokenEmitidoRepository.saveAll(tokens);
    }
    
    public boolean estaRevocado(String token) {
        log.info("Constultado revoque de token: {}", token);
        return tokenEmitidoRepository.findByToken(token)
                .map(TokenEmitido::isRevocado)
                .orElse(true); // Si no está registrado, lo tratamos como inválido
    }
    
    public Long obtenerIdDesdeToken() {
        log.info("Obteniendo id desde token");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof UserAuthDetails userAuthDetails) {
            return userAuthDetails.getId();
        } else {
            throw new PrincipalInvalidoException("Principal no es instancia de UserAuthDetails");
        }
    }
    
    public boolean validarSesionActual(String token, Long idUsuario) {
        try {
            Claims claims = obtenerClaims(token);
            String sub = claims.getSubject(); // el ID fue guardado como String
            return sub != null && sub.equals(idUsuario.toString());
        } catch (Exception e) {
            return false;
        }
    }
    
    private Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}