package com.breakingns.SomosTiendaMas.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RefreshTokenRequest {
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Constructor vacío, getters y setters
}