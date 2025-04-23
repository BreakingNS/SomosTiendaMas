package com.breakingns.SomosTiendaMas.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SesionActivaResponse {
    private Long id;
    private String ip;
    private String userAgent;
    private Instant fechaInicioSesion;
    private boolean revocado;
}