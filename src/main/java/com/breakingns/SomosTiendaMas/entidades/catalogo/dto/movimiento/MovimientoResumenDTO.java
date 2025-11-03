package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovimientoResumenDTO {
    private Long id;
    private Long productoId;
    private TipoMovimientoInventario tipo;
    private Long cantidad;
    private String orderRef;
    private LocalDateTime createdAt;
}