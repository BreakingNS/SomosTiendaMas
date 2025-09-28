package com.breakingns.SomosTiendaMas.entidades.empresa.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter @Setter
public class RegistroPerfilEmpresaDTO {
    private Long idUsuario;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "CUIT inválido (11 dígitos)")
    private String cuit;

    @NotBlank(message = "Condición IVA obligatoria")
    // opcional: validar contra enum con validador custom si lo querés más estricto
    private String condicionIVA;

    @Email(message = "Email empresa inválido")
    @NotBlank(message = "Email de la empresa es obligatorio")
    private String emailEmpresa;

    @NotNull(message = "Indicar si requiere facturación")
    private Boolean requiereFacturacion;

    private Boolean activo;
}
