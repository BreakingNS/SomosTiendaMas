package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OpcionConValoresSimpleDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private String tipo;
    private List<OpcionValorSimpleDTO> valores;
}