package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import java.time.Instant;

public class DatosComprobacionSesionDTO {
    
    private boolean sesionActiva;
    private String username;
    private String rol;
    private Instant jwtExpiracion;
    private Instant refreshExpiracion;

    public DatosComprobacionSesionDTO() {}

    public DatosComprobacionSesionDTO(boolean sesionActiva, String username, String rol, Instant jwtExpiracion, Instant refreshExpiracion) {
        this.sesionActiva = sesionActiva;
        this.username = username;
        this.rol = rol;
        this.jwtExpiracion = jwtExpiracion;
        this.refreshExpiracion = refreshExpiracion;
    }

    public boolean isSesionActiva() { return sesionActiva; }
    public void setSesionActiva(boolean sesionActiva) { this.sesionActiva = sesionActiva; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Instant getJwtExpiracion() { return jwtExpiracion; }
    public void setJwtExpiracion(Instant jwtExpiracion) { this.jwtExpiracion = jwtExpiracion; }

    public Instant getRefreshExpiracion() { return refreshExpiracion; }
    public void setRefreshExpiracion(Instant refreshExpiracion) { this.refreshExpiracion = refreshExpiracion; }
}
