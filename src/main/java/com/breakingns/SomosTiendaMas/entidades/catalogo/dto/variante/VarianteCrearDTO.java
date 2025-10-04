package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import com.breakingns.SomosTiendaMas.entidades.catalogo.validation.SKUValido;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VarianteCrearDTO {
    @NotNull
    private Long productoId;

    @NotNull
    @SKUValido
    private String sku;

    private String codigoBarras;
    private Long pesoGramos;
    private Integer altoMm;
    private Integer anchoMm;
    private Integer largoMm;
    private String metadataJson;
}
