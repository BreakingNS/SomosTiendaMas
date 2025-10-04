package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import com.breakingns.SomosTiendaMas.entidades.catalogo.validation.SKUValido;
import lombok.Data;

@Data
public class VarianteActualizarDTO {
    @SKUValido
    private String sku;

    private String codigoBarras;
    private Long pesoGramos;
    private Integer altoMm;
    private Integer anchoMm;
    private Integer largoMm;
    private String metadataJson;
}
