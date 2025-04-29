package com.breakingns.SomosTiendaMas.auth.utils;

public class TokenUtils {
    
    private TokenUtils() {
        // Constructor privado para evitar instanciación
    }
    
    public static String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return HeaderUtils.extraerAccessToken(authorizationHeader);
        }
        return null;
    }
}
