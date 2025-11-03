package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PrecioProductoResponseDTO {
    private Long id;
    private Long productoId;
    private Long montoCentavos;
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