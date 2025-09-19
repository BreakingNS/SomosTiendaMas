package com.breakingns.SomosTiendaMas.test.Modulo1.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoleHierarchyTest {

    private RoleHierarchyImpl getConfiguredHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
            "ROLE_SUPERADMIN > ROLE_ADMIN\n" +
            "ROLE_ADMIN > ROLE_MODERADOR\n" +
            "ROLE_ADMIN > ROLE_SOPORTE\n" +
            "ROLE_ADMIN > ROLE_ANALISTA\n" +
            "ROLE_MODERADOR > ROLE_EMPRESA\n" +
            "ROLE_MODERADOR > ROLE_USUARIO\n" +
            "ROLE_EMPRESA > ROLE_USUARIO"
        );
    }

    @Test
    void superadminDebeTenerTodosLosPermisos() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_SUPERADMIN"));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOPORTE")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANALISTA")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPRESA")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
    }

    @Test
    void adminDebeTenerPermisosDeModeradorSoporteAnalistaEmpresaUsuario() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_ADMIN"));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOPORTE")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANALISTA")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPRESA")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
    }

    @Test
    void moderadorDebeTenerPermisosDeEmpresaYUsuarioPeroNoAdministrativos() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_MODERADOR"));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPRESA")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
    }

    @Test
    void soporteDebeTenerPermisosDeEmpresaYUsuarioPeroNoAdministrativos() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_SOPORTE"));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPRESA")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
    }

    @Test
    void analistaDebeTenerPermisosDeEmpresaYUsuarioPeroNoAdministrativos() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_ANALISTA"));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPRESA")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
    }

    @Test
    void empresaDebeTenerPermisosDeUsuarioPeroNoAdministrativos() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_EMPRESA"));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR")));
    }

    @Test
    void usuarioNoDebeTenerPermisosDeEmpresaNiAdministrativos() {
        var roleHierarchy = getConfiguredHierarchy();
        var authorities = roleHierarchy.getReachableGrantedAuthorities(List.of(() -> "ROLE_USUARIO"));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPRESA")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
        assertFalse(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MODERADOR")));
    }
}