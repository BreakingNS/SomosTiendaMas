package com.breakingns.SomosTiendaMas.auth.dto.response;

public record AuthResponse(
        String accessToken, 
        String refreshToken
) {}