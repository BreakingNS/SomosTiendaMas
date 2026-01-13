package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VarianteOpcionValorSimpleDTO {
    private Long id;
    private String valor;
    private Integer orden;
}