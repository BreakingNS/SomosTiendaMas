package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PlantillaCategoriaResponseDTO {
    private Long id;
    private Long categoriaId;
    private String nombre;
    private String descripcion;

    // ids de opciones asociadas (útil para UI)
    private List<Long> opcionIds = new ArrayList<>();

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}