package com.breakingns.SomosTiendaMas.auth.utils;

import com.breakingns.SomosTiendaMas.security.exception.TokenNoEncontradoException;

public class HeaderUtils {

    public static String extraerAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenNoEncontradoException("Token de acceso no encontrado o mal formado.");
        }
        return authorizationHeader.substring(7);
    }
}