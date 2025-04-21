package com.breakingns.SomosTiendaMas.auth.security.jwt;

import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
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
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Value("${app.jwt-expiration-ms}")
    private int jwtExpirationMs;
    
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
    
    //Metodo para generar Token
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
}