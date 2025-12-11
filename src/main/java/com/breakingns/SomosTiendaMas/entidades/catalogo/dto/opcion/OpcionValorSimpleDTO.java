package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpcionValorSimpleDTO {
    private Long id;
    private String valor;
    private Integer orden;
}