package com.breakingns.SomosTiendaMas.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank String token,
    @Size(min = 6, max = 16, message = "La contraseña debe tener entre 6 y 16 caracteres.")
    String nuevaPassword
) {}