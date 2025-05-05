package com.breakingns.SomosTiendaMas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OlvidePasswordRequest(
    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "Formato de email inválido.")
    String email
) {}