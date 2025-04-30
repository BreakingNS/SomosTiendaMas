package com.breakingns.SomosTiendaMas.auth.dto;

public record AuthResponse(
        String accessToken, 
        String refreshToken
) {}