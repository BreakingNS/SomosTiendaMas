package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EtiquetaResponseDTO {
    private Long id;
    private String nombre;
    private String slug;

    // audit / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}