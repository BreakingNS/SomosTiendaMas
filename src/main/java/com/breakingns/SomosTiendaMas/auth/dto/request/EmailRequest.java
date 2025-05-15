package com.breakingns.SomosTiendaMas.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    String email
) {}