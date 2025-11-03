package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MovimientoResponseDTO {
    private Long id;
    private Long productoId;
    private TipoMovimientoInventario tipo;
    private Long cantidad;
    private String orderRef;
    private String referenciaId;
    private String metadataJson;

    // auditoría / timing
    private LocalDateTime createdAt;
    private String creadoPor;

    // opcional: balances antes/después si el servicio los calcula
    private Long saldoAntes;
    private Long saldoDespues;
}