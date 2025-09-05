package com.breakingns.SomosTiendaMas.helpers;

import java.util.Map;

import com.breakingns.SomosTiendaMas.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;


public class TokenHelper {

    // Extrae el accessToken de header, cookie o body
    public static String extractAccessToken(HttpServletRequest request, Map<String, String> body) {
        // 1. Header
        String headerToken = request.getHeader("Authorization");
        if (headerToken != null && headerToken.startsWith("Bearer ")) {
            return headerToken.substring(7);
        }
        // 2. Body (opcional, si lo usas)
        if (body != null && body.get("accessToken") != null && !body.get("accessToken").isEmpty()) {
            return body.get("accessToken");
        }
        // 3. Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Extrae el refreshToken de body, cookie o header
    public static String extractRefreshToken(HttpServletRequest request, Map<String, String> body) {
        // 1. Body
        if (body != null && body.get("refreshToken") != null && !body.get("refreshToken").isEmpty()) {
            return body.get("refreshToken");
        }
        // 2. Cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 3. Header (opcional, si lo usas)
        String headerToken = request.getHeader("X-Refresh-Token");
        if (headerToken != null && !headerToken.isEmpty()) {
            return headerToken;
        }
        return null;
    }

    private static JwtTokenProvider jwtTokenProvider;

    public static void setJwtTokenProvider(JwtTokenProvider provider) {
        jwtTokenProvider = provider;
    }

    public static String getUsernameFromRequest(HttpServletRequest request) {
        String token = extractAccessToken(request);
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el access token en la request");
        }
        if (jwtTokenProvider == null) {
            throw new IllegalStateException("JwtTokenProvider no inicializado en TokenHelper");
        }
        return jwtTokenProvider.obtenerUsernameDelToken(token);
    }

    // Ejemplo de método para extraer el token del header/cookie
    public static String extractAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        // Si usas cookies, busca el token en ellas
        // ...código para buscar en cookies...
        return null;
    }
}