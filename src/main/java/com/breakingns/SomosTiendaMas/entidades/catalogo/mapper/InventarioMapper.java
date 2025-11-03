package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.ReservaStockResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioProducto;

import java.util.Objects;

public final class InventarioMapper {

    private InventarioMapper() {}

    public static DisponibilidadResponseDTO toDisponibilidad(Long productoId, long disponible) {
        return new DisponibilidadResponseDTO(productoId, disponible);
    }

    public static ReservaStockResponseDTO toReservaResponse(Long productoId, boolean ok, long disponible) {
        return new ReservaStockResponseDTO(productoId, ok, disponible);
    }

    // ...existing code...
    public static InventarioProducto fromDto(InventarioProductoDTO dto) {
        if (dto == null) return null;
        InventarioProducto e = new InventarioProducto();
        if (dto.getId() != null) e.setId(dto.getId());
        // suponemos que InventarioProducto tiene campos onHand y reserved (Long)
        if (dto.getOnHand() != null) e.setOnHand(dto.getOnHand());
        if (dto.getReserved() != null) e.setReserved(dto.getReserved());
        return e;
    }

    public static InventarioProductoDTO toDto(InventarioProducto e) {
        if (e == null) return null;
        InventarioProductoDTO dto = InventarioProductoDTO.builder().build();
        dto.setId(e.getId());
        dto.setProductoId(e.getProducto() != null ? e.getProducto().getId() : null);
        dto.setOnHand(Objects.requireNonNullElse(e.getOnHand(), 0L));
        dto.setReserved(Objects.requireNonNullElse(e.getReserved(), 0L));
        dto.setDisponible(Math.max(0L, dto.getOnHand() - dto.getReserved()));
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setDeletedAt(e.getDeletedAt());
        dto.setVersion(e.getVersion());
        return dto;
    }
    // ...existing code...
}