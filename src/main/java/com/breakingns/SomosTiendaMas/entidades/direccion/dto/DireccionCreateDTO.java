package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionCreateDTO {
    @NotBlank
    private String calle;

    private String numero;
    private String piso;
    private String departamento;
    private String codigoPostal;
    private String localidad;
    private Boolean esPrincipal = false;

    private String notas;
}
