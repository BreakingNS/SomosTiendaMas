package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private Long categoriaPadreId;

    // Añadido para respuestas auditables
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // opcional: número de hijos para listas
    private Integer hijosCount;
}