package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MovimientoCrearDTO {
    @NotNull
    private Long productoId;

    @NotNull
    private TipoMovimientoInventario tipo;

    @NotNull
    @Min(1)
    private Long cantidad;

    @Size(max = 120)
    private String orderRef;

    @Size(max = 120)
    private String referenciaId;

    // JSON libre para metadata (opcional)
    private String metadataJson;
}