package com.breakingns.SomosTiendaMas.security.config;

public class PublicRoutes {
    public static final String[] RUTAS_PUBLICAS = {
        // Rutas API públicas
        "/api/admin/**",
        "/api/auth/public/**",
        "/api/direccion/public/**",
        "/api/gestionusuario/public/usuario/**",
        "/api/gestionusuario/public/empresa/**",
        "/api/gestionusuario/public/verificar-email",
        "/api/gestionusuario/public/verificar-email/**",
        "/api/import-ubicaciones/**",
        "/api/password/public/**",
        "/api/perfil-empresa/public",
        "/api/registro/public/**",
        "/api/sesiones/public/**",
        "/debug/**",
        "/test/**",
        "/test/api/auth/public/**",
        "/api/telefono/public/**",
        "/api/import-codigos-area/**",
        "/api/sesiones/public/**",
        "/api/sesiones/public/verificar-email/**",
        "/api/test/public/**",
        "/api/perfilUsuario/public/**",
        "/api/perfilEmpresa/public/**",
        "/api/usuario/public/**",

        // Rutas públicas para archivos estáticos
        
        "/images/productos/**",
        "/consultaRegistros.html",
        "/registro_usuario.html",
        "/registro_empresa.html",
        "/css/**",
        "/favicon.ico",
        "/images/**",
        "/index.html",
        "/js/**",
        "/login.html",
        "/main.html",
        "/recuperar.html",
        "/registro.html",
        "/registroCompleto.html",
        "/reset-password.html",
        "/verificar-email.html",
        "/public/**",
        "/"
    };
}
