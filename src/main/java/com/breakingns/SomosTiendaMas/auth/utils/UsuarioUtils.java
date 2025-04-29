package com.breakingns.SomosTiendaMas.auth.utils;

import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;

public class UsuarioUtils {
    
    private UsuarioUtils() {
        // Constructor privado para evitar instanciación
    }

    public static boolean existeUsuario(String token, JwtTokenProvider jwtTokenProvider) {
        return jwtTokenProvider.validarToken(token);
    }
    
}
