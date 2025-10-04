package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpcionProductoResponseDTO {
    private Long id;
    private Long productoId;
    private String nombre;
    private Integer orden;
}
