package com.breakingns.SomosTiendaMas.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public AuthResponse() {
    }
    
    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters y setters (o anotaciones @Data de Lombok si las usás)
    
}
