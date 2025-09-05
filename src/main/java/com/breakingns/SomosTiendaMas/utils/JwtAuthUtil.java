package com.breakingns.SomosTiendaMas.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class JwtAuthUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Responde con 401 para no autenticado
    public static void noAutenticado(HttpServletResponse response, String mensaje) throws IOException {
        enviarRespuesta(response, HttpServletResponse.SC_UNAUTHORIZED, mensaje);
    }

    // Responde con 401 para no autorizado (ajuste)
    public static void noAutorizado(HttpServletResponse response, String mensaje) throws IOException {
        enviarRespuesta(response, HttpServletResponse.SC_UNAUTHORIZED, mensaje); // Cambié a 401
    }

    // Rechaza el acceso con un mensaje
    public static void rechazar(HttpServletResponse response, String mensaje) throws IOException {
        enviarRespuesta(response, HttpServletResponse.SC_BAD_REQUEST, mensaje);
    }

    // Método general para enviar respuestas
    private static void enviarRespuesta(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        String json = objectMapper.writeValueAsString(Map.of(
            "error", "Token inválido o expirado", // Se puede cambiar por algo más genérico si lo necesitas
            "message", mensaje, 
            "timestamp", Instant.now().toString()
        ));
        response.getWriter().write(json);
    }
}