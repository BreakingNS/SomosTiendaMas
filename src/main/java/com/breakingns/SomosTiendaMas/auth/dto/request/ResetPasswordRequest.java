package com.breakingns.SomosTiendaMas.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "El token es obligatorio")
    String token,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres.")
    @Pattern(
        regexp = "^(?=\\S+$).{8,128}$",
        message = "La contraseña no debe contener espacios."
    )
    String nuevaPassword
) {}