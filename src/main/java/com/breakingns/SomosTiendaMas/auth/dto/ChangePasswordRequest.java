package com.breakingns.SomosTiendaMas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "La contraseña actual es requerida")
    String currentPassword,

    @Size(min = 6, max = 16, message = "La nueva contraseña debe tener entre 6 y 16 caracteres")
    String newPassword
) {}