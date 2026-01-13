package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrecioVarianteCrearDTO {
    private Long varianteId; // ahora el precio se asocia a una variante (se toma desde la ruta)

    @NotNull
    @Min(0)
    private Long montoCentavos; // precio final ingresado (con IVA)

    // opcional: permitir proporcionar precio anterior (para mostrar tachado)
    @Min(0)
    private Long precioAnteriorCentavos;

    // opcional: precio sin IVA (si no se provee, se calculará en el service)
    @Min(0)
    private Long precioSinIvaCentavos;

    // opcional: tasa de IVA aplicada; si es null usar valor por defecto/config
    private Integer ivaPorcentaje;

    private Moneda moneda;

    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;

    // opcional: crear como activo o no
    private Boolean activo;
}
