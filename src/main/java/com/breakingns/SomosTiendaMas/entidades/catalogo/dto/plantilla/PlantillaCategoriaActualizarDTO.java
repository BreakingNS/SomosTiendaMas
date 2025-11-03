package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PlantillaCategoriaActualizarDTO {
    @Size(max = 160)
    private String nombre;

    private String descripcion;

    // Reemplaza la lista de opciones asociadas (si es null no tocar)
    private List<Long> opcionIds;
}