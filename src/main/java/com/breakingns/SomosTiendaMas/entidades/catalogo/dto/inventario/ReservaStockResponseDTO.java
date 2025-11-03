package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservaStockResponseDTO {
    private Long productoId;
    private boolean ok;
    private long disponible;
}