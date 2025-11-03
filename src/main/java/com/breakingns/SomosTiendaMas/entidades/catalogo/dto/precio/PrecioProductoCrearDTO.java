package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrecioProductoCrearDTO {
    @NotNull
    private Long productoId;

    @NotNull
    @Min(0)
    private Long montoCentavos;

    private Moneda moneda;

    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;

    // opcional: crear como activo o no
    private Boolean activo;
}