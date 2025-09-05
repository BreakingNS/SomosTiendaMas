package com.breakingns.SomosTiendaMas.security.config;

public class PublicRoutes {
    public static final String[] RUTAS_PUBLICAS = {
        "/api/auth/public/**",
        "/api/password/public/**",
        "/api/registro/public/**",
        "/api/sesiones/public/**",
        "/api/perfil-empresa/public",
        "/test/api/auth/public/**",
        "/api/direccion/public/**",
        "/api/admin/**",
        "/test/**",
        "/api/gestionusuario/public/usuario/**",
        "/api/gestionusuario/public/verificar-email/**",
        "/debug/**",
        // Rutas públicas para archivos estáticos
        "/registroCompleto.html",
        "/consultaRegistros.html",
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
