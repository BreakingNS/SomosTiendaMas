package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarcaResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
}
