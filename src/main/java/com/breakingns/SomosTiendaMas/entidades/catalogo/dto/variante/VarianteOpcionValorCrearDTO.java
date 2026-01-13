package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VarianteOpcionValorCrearDTO {
    @NotNull
    private Long opcionId; // obligatorio si se crea por endpoint separado

    @NotBlank
    @Size(max = 120)
    private String valor;

    @Size(max = 160)
    private String slug;

    private Integer orden = 0;
}