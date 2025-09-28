package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VerificarEmailDTO {
    @Email(message = "Email inválido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @NotBlank(message = "El código de verificación no puede estar vacío")
    private String codigoVerificacion;
}
