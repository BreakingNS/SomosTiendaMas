package com.breakingns.SomosTiendaMas.auth.security.jwt;

import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-ms}")
    private int jwtExpirationMs;

    // Método para convertir el String jwtSecret a una Key segura
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
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
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)   // Lo firma con una clave secreta
            .compact();
    }

    public String obtenerUsernameDelToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
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
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }
    
    public List<String> obtenerRolesDelToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
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