package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrecioProductoResumenDTO {
    private Long id;
    private Long productoId;
    private Long montoCentavos;
    private Moneda moneda;
    private Boolean activo;
    private LocalDateTime vigenciaDesde;
}