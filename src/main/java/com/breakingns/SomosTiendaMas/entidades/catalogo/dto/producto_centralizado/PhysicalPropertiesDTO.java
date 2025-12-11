package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalPropertiesDTO {
    // Peso neto en gramos
    private Integer weightGrams;

    // Dimensiones en milímetros
    private Integer widthMm;
    private Integer heightMm;
    private Integer depthMm;

    // Peso y dimensiones del paquete (para envíos)
    private Integer packageWeightGrams;
    private Integer packageWidthMm;
    private Integer packageHeightMm;
    private Integer packageDepthMm;
}