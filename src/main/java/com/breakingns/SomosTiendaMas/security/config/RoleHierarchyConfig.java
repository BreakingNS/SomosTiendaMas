package com.breakingns.SomosTiendaMas.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleHierarchyConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
            "ROLE_SUPERADMIN > ROLE_ADMIN\n" +
            "ROLE_ADMIN > ROLE_MODERADOR\n" +
            "ROLE_ADMIN > ROLE_SOPORTE\n" +
            "ROLE_ADMIN > ROLE_ANALISTA\n" +
            "ROLE_MODERADOR > ROLE_EMPRESA\n" +
            "ROLE_MODERADOR > ROLE_USUARIO\n" +
            "ROLE_EMPRESA > ROLE_USUARIO"
        );
        return hierarchy;
    }
}