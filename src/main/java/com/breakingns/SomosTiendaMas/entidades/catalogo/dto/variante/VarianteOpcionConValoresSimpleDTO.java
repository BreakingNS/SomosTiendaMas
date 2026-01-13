package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VarianteOpcionConValoresSimpleDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private String tipo;
    private List<VarianteOpcionValorSimpleDTO> valores;
}