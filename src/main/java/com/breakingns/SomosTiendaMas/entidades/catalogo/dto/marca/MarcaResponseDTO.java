package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MarcaResponseDTO {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;

    // auditoría / soft-delete
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // moderación / creación
    private Boolean creadaPorUsuario;
    private Long creadaPorVendedorId;
    private String estadoModeracion;
    private String moderacionNotas;
    private String moderadoPor;
    private LocalDateTime moderadoEn;

    // relaciones útiles
    private List<Long> categoriaIds = new ArrayList<>();
    private Integer productosCount;
}