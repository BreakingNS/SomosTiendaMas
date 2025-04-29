package com.breakingns.SomosTiendaMas.auth.utils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    
    public static String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isBlank()) ? request.getRemoteAddr() : ip;
    }

}
