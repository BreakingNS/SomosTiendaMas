package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VendedorResponseDTO {
    private Long id;
    private Long userId;
    private String nombre;
    private String descripcion;
    private Double rating;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long version;
}