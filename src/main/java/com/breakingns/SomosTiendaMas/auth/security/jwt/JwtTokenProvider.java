package com.breakingns.SomosTiendaMas.auth.security.jwt;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
import com.breakingns.SomosTiendaMas.auth.utils.RsaKeyUtil;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    private final IUsuarioRepository usuarioRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final ITokenEmitidoRepository tokenEmitidoRepository;

    @Value("${app.jwt-expiration-ms}")
    private int jwtExpirationMs;

    public JwtTokenProvider(
            IUsuarioRepository usuarioRepository,
            UserDetailsServiceImpl userDetailsService,
            ITokenEmitidoRepository tokenEmitidoRepository,
            RsaKeyUtil rsaKeyUtil
    ) throws Exception {
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.privateKey = rsaKeyUtil.loadPrivateKey("src/main/resources/keys/private.pem");
        this.publicKey = rsaKeyUtil.loadPublicKey("src/main/resources/keys/public.pem");
    }

    public String generarTokenDesdeAuthentication(Authentication authentication) {
        UserAuthDetails userPrincipal = (UserAuthDetails) authentication.getPrincipal();
        return generarTokenParaUsuario(userPrincipal.getId(), userPrincipal.getUsername());
    }

    public String generarTokenConUsuario(Usuario usuario) {
        return generarTokenParaUsuario(usuario.getIdUsuario(), usuario.getUsername());
    }

    public String generarTokenDesdeUsername(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return generarTokenDesdeAuthentication(auth);
    }

    private String generarTokenParaUsuario(Long id, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        // Guardar el token emitido
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado al generar token"));

        TokenEmitido tokenEmitido = new TokenEmitido();
        tokenEmitido.setToken(token);
        tokenEmitido.setRevocado(false);
        tokenEmitido.setFechaEmision(now.toInstant());
        tokenEmitido.setFechaExpiracion(expiryDate.toInstant());
        tokenEmitido.setUsuario(usuario);

        tokenEmitidoRepository.save(tokenEmitido);
        return token;
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public String obtenerUsernameDelToken(String token) {
        return obtenerClaims(token).get("username", String.class);
    }

    public Long obtenerIdDelToken(String token) {
        return Long.valueOf(obtenerClaims(token).getSubject());
    }

    public List<String> obtenerRolesDelToken(String token) {
        Object roles = obtenerClaims(token).get("roles");
        if (roles instanceof List<?> lista) {
            return lista.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    public Instant obtenerFechaExpiracion(String token) {
        return obtenerClaims(token).getExpiration().toInstant();
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}