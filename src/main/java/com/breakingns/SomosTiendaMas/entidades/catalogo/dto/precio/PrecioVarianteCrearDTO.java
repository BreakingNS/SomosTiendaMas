package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrecioVarianteCrearDTO {
    @NotNull
    private Long varianteId;

    @Min(0)
    private Long montoCentavos;
}
