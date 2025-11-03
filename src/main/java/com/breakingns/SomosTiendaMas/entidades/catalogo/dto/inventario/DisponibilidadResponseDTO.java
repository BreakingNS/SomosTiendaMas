package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadResponseDTO {
    private Long productoId;
    private long disponible;
}