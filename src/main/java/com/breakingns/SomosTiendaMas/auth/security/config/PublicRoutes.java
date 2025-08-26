package com.breakingns.SomosTiendaMas.auth.security.config;

public class PublicRoutes {
    public static final String[] RUTAS_PUBLICAS = {
        "/api/auth/public/**",
        "/api/password/public/**",
        "/api/registro/public/**",
        "/api/sesiones/public/**",
        "/test/api/auth/public/**",
        "/test/**",
        // Rutas públicas para archivos estáticos
        "/index.html",
        "/main.html",
        "/login.html",
        "/registro.html",
        "/recuperar.html",
        "/css/**",
        "/js/**",
        "/images/**",
        "/"
    };
}
