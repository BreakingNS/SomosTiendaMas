package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioProductoDTO {
    private Long id;
    private Long productoId;
    private Integer onHand;
    private Integer reserved;
    private Integer disponible; // onHand - reserved (para UI)
    private Long almacenId;

    // trazabilidad / auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // version para optimistic locking
    private Long version;
}
