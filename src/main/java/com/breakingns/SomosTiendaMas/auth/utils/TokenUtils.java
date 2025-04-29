package com.breakingns.SomosTiendaMas.auth.utils;

import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TokenUtils {
    
    private TokenUtils() {
        // Constructor privado para evitar instanciación
    }

    public static boolean validarToken(String token, JwtTokenProvider jwtTokenProvider) {
        return jwtTokenProvider.validarToken(token);
    }

    public static ResponseEntity<?> respuestaTokenInvalido() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token inválido o expirado"));
    }
}
