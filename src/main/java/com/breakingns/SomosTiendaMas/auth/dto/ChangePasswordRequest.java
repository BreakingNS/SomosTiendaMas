package com.breakingns.SomosTiendaMas.auth.dto;

public record ChangePasswordRequest(
        String currentPassword, 
        String newPassword
) {}