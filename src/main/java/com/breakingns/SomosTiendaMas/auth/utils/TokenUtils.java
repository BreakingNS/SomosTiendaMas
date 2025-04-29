package com.breakingns.SomosTiendaMas.auth.utils;

import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;

public class TokenUtils {
    
    private TokenUtils() {
        // Constructor privado para evitar instanciación
    }

    public static boolean validarToken(String token, JwtTokenProvider jwtTokenProvider) {
        return jwtTokenProvider.validarToken(token);
    }
    
    public static String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return HeaderUtils.extraerAccessToken(authorizationHeader);
        }
        return null;
    }
}
