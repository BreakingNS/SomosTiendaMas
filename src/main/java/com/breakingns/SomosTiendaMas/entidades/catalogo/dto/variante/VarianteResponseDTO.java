package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VarianteResponseDTO {
    private Long id;
    private Long productoId;
    private String sku;
    private String codigoBarras;
    private Long pesoGramos;
    private Integer altoMm;
    private Integer anchoMm;
    private Integer largoMm;
    private String metadataJson;
}
