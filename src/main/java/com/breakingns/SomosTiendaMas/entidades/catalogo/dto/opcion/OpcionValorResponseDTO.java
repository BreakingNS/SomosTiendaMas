package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OpcionValorResponseDTO {
    private Long id;
    private Long opcionId;
    private String valor;
    private String slug;
    private Integer orden;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}