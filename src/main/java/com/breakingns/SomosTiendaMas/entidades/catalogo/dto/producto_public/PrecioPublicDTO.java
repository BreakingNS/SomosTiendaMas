package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PrecioPublicDTO {
    private Long montoCentavos;
    private Long precioAnteriorCentavos;
    private BigDecimal descuentoPorcentaje;
    private Moneda moneda;
    private Long precioSinIvaCentavos;
    private Integer ivaPorcentaje;
    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;
}
