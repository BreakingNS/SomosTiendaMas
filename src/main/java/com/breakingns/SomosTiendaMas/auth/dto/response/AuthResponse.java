package com.breakingns.SomosTiendaMas.auth.dto.response;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public AuthResponse(String accessToken, String refreshToken){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
    // getters y setters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}