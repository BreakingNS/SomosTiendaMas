package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria;

import lombok.Data;

@Data
public class CategoriaActualizarDTO {
    private String nombre;
    private String slug;
    private String descripcion;
    private Long categoriaPadreId;
}
