package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OpcionValorActualizarDTO {
    @Size(max = 120)
    private String valor;

    @Size(max = 160)
    private String slug;

    private Integer orden;
}