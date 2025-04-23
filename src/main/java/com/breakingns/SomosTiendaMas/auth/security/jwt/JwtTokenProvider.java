package com.breakingns.SomosTiendaMas.auth.security.jwt;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.service.UserDetailsServiceImpl;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import static org.bouncycastle.crypto.params.Blake3Parameters.key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@Component
public class JwtTokenProvider {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${app.jwt-expiration-ms}")
    private int jwtExpirationMs;
    
    @Autowired
    private IUsuarioRepository usuarioRepository;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private ITokenEmitidoRepository tokenEmitidoRepository;
    
    public JwtTokenProvider() throws Exception {
        this.privateKey = loadPrivateKey("src/main/resources/keys/private.pem");
        this.publicKey = loadPublicKey("src/main/resources/keys/public.pem");
    }
    
    // Método para cargar clave privada
    private PrivateKey loadPrivateKey(String filepath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filepath));
        String privateKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(privateKeyPEM));
        return keyFactory.generatePrivate(keySpec);
    }
    
    // Método para cargar clave publica
    private PublicKey loadPublicKey(String filepath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filepath));
        String publicKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(java.util.Base64.getDecoder().decode(publicKeyPEM));
        return keyFactory.generatePublic(keySpec);
    }
    /*
    //Metodo para generar Token por Authentication
    public String generarToken(Authentication authentication) {//Recibe el objeto Authentication con los datos validados por autenticacion.
        
        UserAuthDetails userPrincipal = (UserAuthDetails) authentication.getPrincipal();
        //Obtiene los datos del usuario logueado (que implementa UserDetails) desde el objeto Authentication.

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        //Guarda la fecha actual y la fecha de expiración del token (por ejemplo, 1 hora después).

        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())                // El "dueño" del token
            .claim("id", userPrincipal.getId())                   // Agrega un dato extra: el ID
            .setIssuedAt(now)                                         // Cuándo fue emitido
            .setExpiration(expiryDate)                                // Cuándo expira
            .signWith(privateKey, SignatureAlgorithm.RS256)         // Lo firma con una clave privada
            .compact();
    }
    */
    public String generarToken(Authentication authentication) {
        UserAuthDetails userPrincipal = (UserAuthDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("id", userPrincipal.getId())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact();

        // Guardar el token emitido en la base de datos
        TokenEmitido tokenEmitido = new TokenEmitido();
        tokenEmitido.setToken(token);
        tokenEmitido.setRevocado(false);
        tokenEmitido.setFechaEmision(now.toInstant());
        tokenEmitido.setFechaExpiracion(expiryDate.toInstant());

        // Asegúrate de asignar el usuario correctamente
        Usuario usuario = usuarioRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado al generar token"));
        tokenEmitido.setUsuario(usuario);

        // Guardar el token en la base de datos
        tokenEmitidoRepository.save(tokenEmitido);

        return token;
    }
    
    public String generarTokenDesdeUsername(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        return generarToken(auth); // usás tu método existente
    }

    public String obtenerUsernameDelToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException |
                 MalformedJwtException | SignatureException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public Long obtenerIdDelToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }
    
    public List<String> obtenerRolesDelToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object roles = claims.get("roles");
        if (roles instanceof List<?> lista) {
            return lista.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }
    
    public Instant obtenerFechaExpiracion(String token) {
        Claims claims = obtenerClaims(token);
        Date fecha = claims.getExpiration();
        return fecha.toInstant();
    }
    
    private Claims obtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey) // tu clave pública si usás RSA
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

}