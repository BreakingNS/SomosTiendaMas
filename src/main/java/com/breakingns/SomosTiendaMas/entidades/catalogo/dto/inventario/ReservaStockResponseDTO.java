package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservaStockResponseDTO {
    private Long varianteId;
    private boolean ok;
    private long disponible;
}