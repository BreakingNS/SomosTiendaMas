package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VarianteOpcionValorActualizarDTO {
    @Size(max = 120)
    private String valor;

    @Size(max = 160)
    private String slug;

    private Integer orden;
}