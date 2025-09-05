package com.breakingns.SomosTiendaMas;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
import com.breakingns.SomosTiendaMas.utils.RsaKeyUtil;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import java.util.UUID;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@Component
public class JwtTokenProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProviderTest.class);

    @Value("${jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    private final RsaKeyUtil rsaKeyUtil;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private final IUsuarioRepository usuarioRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final ITokenEmitidoRepository tokenEmitidoRepository;

    private int jwtExpirationMs = 2000;

    @PostConstruct
    public void init() throws Exception {
        System.out.println("Ruta clave privada: " + privateKeyPath);
        System.out.println("Ruta clave pública: " + publicKeyPath);
        this.privateKey = rsaKeyUtil.loadPrivateKey(privateKeyPath);
        this.publicKey = rsaKeyUtil.loadPublicKey(publicKeyPath);
    }

    public JwtTokenProviderTest(
            IUsuarioRepository usuarioRepository,
            UserDetailsServiceImpl userDetailsService,
            ITokenEmitidoRepository tokenEmitidoRepository,
            RsaKeyUtil rsaKeyUtil
    ) {
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.rsaKeyUtil = rsaKeyUtil;
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

        // Obtener roles del usuario
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado al generar token"));

        List<String> roles = usuario.getRoles().stream()
            .map(rol -> rol.getNombre().name()) // o solo getNombre() si ya es string
            .toList();

        String token = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // typ: JWT
                .setSubject(String.valueOf(id))
                .claim("username", username)
                .claim("roles", roles)
                .claim("jti", UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

        // Guardar el token emitido
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
            // Parsear el JWT
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            // Verificar si el token está expirado
            Date expirationDate = claimsJws.getBody().getExpiration();
            if (expirationDate.before(new Date())) {
                logger.warn("Token expirado: {}", token);
                return false;
            }

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

    public boolean validarTokenPorId(String token, Long idUsuario) {
        try {
            // Verificar si el token es válido (sin expiración)
            if (!validarToken(token)) {
                return false;
            }

            // Extraer el ID del usuario del token
            Long idDelToken = obtenerIdDesdeToken(token);

            // Verificar si el ID extraído del token coincide con el ID proporcionado
            return idDelToken.equals(idUsuario);
        } catch (Exception e) {
            // Manejar cualquier error en la validación
            log.error("Error validando el token", e);
            return false;
        }
    }
    
    public Long obtenerIdDesdeToken(String token) {
        Claims claims = obtenerClaims(token); // Usa tu método que funciona correctamente
        String sub = claims.getSubject();     // getSubject() es equivalente a claims.get("sub", String.class)
        return Long.valueOf(sub);           // Conversión manual
    }
}