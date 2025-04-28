package com.breakingns.SomosTiendaMas.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequest {
    private String token;
    private String nuevaPassword;

    // getters y setters

    public ResetPasswordRequest() {
    }
    
    public ResetPasswordRequest(String token, String nuevaPassword) {
        this.token = token;
        this.nuevaPassword = nuevaPassword;
    }
}