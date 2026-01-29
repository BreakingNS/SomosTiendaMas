package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaStockRequestDTO {
    // varianteId proviene de la ruta; no validarlo aquí para permitir que el controlador lo asigne
    private Long varianteId;

    @Min(1)
    private long cantidad;

    @NotNull
    private String orderRef;
}