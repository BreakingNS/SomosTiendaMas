package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrecioProductoActualizarDTO {
    @Min(0)
    private Long montoCentavos;

    // editable: precio anterior (nullable)
    @Min(0)
    private Long precioAnteriorCentavos;

    // editable: precio sin IVA (nullable)
    @Min(0)
    private Long precioSinIvaCentavos;

    // editable: iva aplicado (nullable)
    private Integer ivaPorcentaje;

    private Moneda moneda;

    private LocalDateTime vigenciaDesde;
    private LocalDateTime vigenciaHasta;

    private Boolean activo;

    @Size(max = 120)
    private String creadoPor; // si se permite editar este campo
}