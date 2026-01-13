package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
public class PrecioVarianteResponseDTO {
    private Long id;
    private Long productoId;
    private Long montoCentavos;               // precio final (con IVA)
    private Long precioAnteriorCentavos;      // nuevo: precio anterior (nullable)
    private Long precioSinIvaCentavos;        // nuevo: precio sin IVA (nullable)
    private Integer ivaPorcentaje;            // nuevo: tasa de IVA aplicada (nullable)
    private BigDecimal descuentoPorcentaje;   // nuevo: campo derivado para la UI (nullable)
    private Moneda moneda;
    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;
    private Boolean activo;
    private String creadoPor;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
