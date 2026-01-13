package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VarianteOpcionValorResponseDTO {
    private Long id;
    private Long opcionId;
    private String valor;
    private Integer orden;
}