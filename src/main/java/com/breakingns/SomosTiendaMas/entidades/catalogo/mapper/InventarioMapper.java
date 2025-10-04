package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.ReservaStockResponseDTO;

public class InventarioMapper {

    public static DisponibilidadResponseDTO toDisponibilidad(Long varianteId, long disponible) {
        return new DisponibilidadResponseDTO(varianteId, disponible);
    }

    public static ReservaStockResponseDTO toReservaResponse(boolean ok, long disponible) {
        return new ReservaStockResponseDTO(ok, disponible);
    }
}
