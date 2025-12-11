package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OpcionConValoresDTO {
    private Long id;
    private Long productoId; // puede ser null para plantillas
    private String nombre;
    private Integer orden;
    private String tipo;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // lista de valores asociados
    private List<OpcionValorResponseDTO> valores;
}
