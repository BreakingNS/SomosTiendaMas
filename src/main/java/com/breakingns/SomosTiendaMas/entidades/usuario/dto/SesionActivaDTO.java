package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import java.time.Instant;

public record SesionActivaDTO(
    Long id,
    String ip,
    String userAgent,
    Instant fechaInicioSesion,
    Instant fechaExpiracion,
    boolean revocado
    
) {
    // Compatibilidad con código/tests que esperan isRevocado()
    public boolean isRevocado() {
        return revocado;
    }
}