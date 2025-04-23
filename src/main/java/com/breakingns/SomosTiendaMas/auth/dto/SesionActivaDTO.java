package com.breakingns.SomosTiendaMas.auth.dto;

import java.time.Instant;

public record SesionActivaDTO(
    Long id,
    String ip,
    String userAgent,
    Instant fechaInicioSesion,
    Instant fechaExpiracion,
    boolean revocado
) {}