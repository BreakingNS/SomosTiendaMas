package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MarcaActualizarDTO {
    @Size(max = 160)
    private String nombre;

    @Size(max = 180)
    private String slug;

    private String descripcion;

    // Para actualizaciones administrativas se pueden exponer en otro DTO separado:
    // private EstadoModeracion estadoModeracion;
    // private String moderacionNotas;
}