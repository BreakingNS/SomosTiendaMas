package com.breakingns.SomosTiendaMas.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "El token es obligatorio")
    String token,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, max = 16, message = "La contraseña debe tener entre 6 y 16 caracteres.")
    @Pattern(
        regexp = "^(?=\\S+$).{6,16}$",
        message = "La contraseña no debe contener espacios."
    )
    String nuevaPassword
) {}