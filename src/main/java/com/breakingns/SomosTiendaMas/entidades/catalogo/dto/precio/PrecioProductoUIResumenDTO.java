package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrecioProductoUIResumenDTO {
    private Long productoId;
    private Long montoCentavos;              // precio final (con IVA)
    private Long precioAnteriorCentavos;     // opcional (tachado)
    private Long precioSinIvaCentavos;       // opcional
    private Integer ivaPorcentaje;           // opcional
    private BigDecimal descuentoPorcentaje;  // opcional
    private Moneda moneda;
    private Boolean activo;
}
