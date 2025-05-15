package com.breakingns.SomosTiendaMas.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Pattern(
        regexp = "^[a-zA-Z0-9_.-]{4,30}$",
        message = "El nombre de usuario solo puede contener letras, números, puntos, guiones o guiones bajos (4-30 caracteres)"
    )
    String username,

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^\\S{6,16}$",
        message = "La contraseña debe tener entre 6 y 16 caracteres sin espacios"
    )
    String password
    
) {}