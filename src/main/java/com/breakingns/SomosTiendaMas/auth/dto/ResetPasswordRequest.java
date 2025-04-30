package com.breakingns.SomosTiendaMas.auth.dto;

public record ResetPasswordRequest (
        String token, 
        String nuevaPassword
) {}