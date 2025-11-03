package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagenProductoDTO {
    private Long id;
    private Long productoId;
    private String url;
    private String alt;
    private Integer orden;
}
