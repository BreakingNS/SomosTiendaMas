package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long categoriaPadreId;
}
