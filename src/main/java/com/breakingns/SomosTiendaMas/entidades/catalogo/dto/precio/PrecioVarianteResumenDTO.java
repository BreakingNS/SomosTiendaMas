package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class PrecioVarianteResumenDTO {
    private Long id;
    private Long productoId;
    private Long montoCentavos;
    private Long precioAnteriorCentavos;    // mostrar precio tachado en listados
    private BigDecimal descuentoPorcentaje; // derivado opcional para UI
    private Moneda moneda;
    private Boolean activo;
    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;
    private Long precioSinIvaCentavos;
    private Integer ivaPorcentaje;
}
