package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.ReservaStockResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioProducto;

public final class InventarioMapper {

    private InventarioMapper() {}

    public static DisponibilidadResponseDTO toDisponibilidad(Long productoId, long disponible) {
        return new DisponibilidadResponseDTO(productoId, disponible);
    }

    public static ReservaStockResponseDTO toReservaResponse(Long productoId, boolean ok, long disponible) {
        return new ReservaStockResponseDTO(productoId, ok, disponible);
    }

    public static InventarioProducto fromDto(InventarioProductoDTO dto) {
        if (dto == null) return null;
        InventarioProducto e = new InventarioProducto();
        if (dto.getId() != null) e.setId(dto.getId());
        // NOTA: no seteamos producto aquí (se debe enlazar en el service si corresponde)
        if (dto.getOnHand() != null) e.setOnHand(dto.getOnHand());
        if (dto.getReserved() != null) e.setReserved(dto.getReserved());
        if (dto.getAlmacenId() != null) e.setAlmacenId(dto.getAlmacenId());
        // trazabilidad si la entidad tiene campos
        if (dto.getCreatedAt() != null) e.setCreatedAt(dto.getCreatedAt());
        if (dto.getUpdatedAt() != null) e.setUpdatedAt(dto.getUpdatedAt());
        if (dto.getVersion() != null) e.setVersion(dto.getVersion());
        return e;
    }

    @SuppressWarnings("unused")
    private static boolean hasMethodSetDeletedAt(InventarioProducto e) {
        // helper para evitar errores si la entidad no tiene setDeletedAt;
        // Esta función se evalúa en tiempo de compilación solo por presencia del método en la entidad.
        // Si tu entidad NO tiene deletedAt elimina las referencias a deletedAt en fromDto/toDto en lugar de usar este helper.
        try {
            e.getClass().getMethod("setDeletedAt", java.time.LocalDateTime.class);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    public static InventarioProductoDTO toDto(InventarioProducto e) {
        if (e == null) return null;
        InventarioProductoDTO dto = InventarioProductoDTO.builder().build();
        dto.setId(e.getId());
        dto.setProductoId(e.getProducto() != null ? e.getProducto().getId() : null);

        // Normalizar nulls a 0 (Integer)
        Integer onHand = e.getOnHand() != null ? e.getOnHand() : 0;
        Integer reserved = e.getReserved() != null ? e.getReserved() : 0;

        dto.setOnHand(onHand);
        dto.setReserved(reserved);
        dto.setDisponible(Math.max(0, onHand - reserved)); // devuelve int, DTO acepta Integer
        dto.setAlmacenId(e.getAlmacenId());

        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        // si tu entidad tiene deletedAt:
        // dto.setDeletedAt(e.getDeletedAt());
        dto.setVersion(e.getVersion());
        return dto;
    }
}