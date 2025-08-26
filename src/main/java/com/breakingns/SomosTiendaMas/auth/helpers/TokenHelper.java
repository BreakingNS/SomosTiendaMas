package com.breakingns.SomosTiendaMas.auth.helpers;

import java.util.Map;

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
}