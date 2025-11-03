package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Data;

@Data
public class OpcionResumenDTO {
    private Long id;
    private String nombre;
    private Integer orden;
    private String tipo;
}