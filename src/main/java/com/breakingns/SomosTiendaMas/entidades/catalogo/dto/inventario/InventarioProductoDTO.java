package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioProductoDTO {
    private Long id;
    private Long productoId;
    private Long onHand;
    private Long reserved;
    private Long disponible; // onHand - reserved (puede rellenarse en el mapper)
    // auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    // versión para optimistic locking
    private Long version;
}
