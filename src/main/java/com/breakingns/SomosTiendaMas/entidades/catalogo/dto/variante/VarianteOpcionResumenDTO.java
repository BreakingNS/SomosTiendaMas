package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import lombok.Data;

@Data
public class VarianteOpcionResumenDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private String tipo;
}