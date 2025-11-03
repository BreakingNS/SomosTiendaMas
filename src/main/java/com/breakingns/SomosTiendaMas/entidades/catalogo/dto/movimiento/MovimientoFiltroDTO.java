package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovimientoFiltroDTO {
    private Long productoId;
    private String orderRef;
    private TipoMovimientoInventario tipo;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private Integer page;
    private Integer size;
}