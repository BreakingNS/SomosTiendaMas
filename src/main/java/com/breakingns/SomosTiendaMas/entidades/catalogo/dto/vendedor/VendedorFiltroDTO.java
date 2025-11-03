package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor;

import lombok.Data;

@Data
public class VendedorFiltroDTO {
    private String nombreContains;
    private Double minRating;
    private Integer page;
    private Integer size;
}