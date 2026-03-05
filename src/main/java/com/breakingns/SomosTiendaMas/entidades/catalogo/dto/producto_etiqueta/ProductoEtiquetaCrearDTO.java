package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_etiqueta;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoEtiquetaCrearDTO {
    @NotNull
    private Long productoId;

    @NotNull
    private Long etiquetaId;
}
