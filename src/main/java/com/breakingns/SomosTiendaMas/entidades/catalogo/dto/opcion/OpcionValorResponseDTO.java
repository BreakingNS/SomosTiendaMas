package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpcionValorResponseDTO {
    private Long id;
    private Long opcionId;
    private String valor;
    private Integer orden;
}