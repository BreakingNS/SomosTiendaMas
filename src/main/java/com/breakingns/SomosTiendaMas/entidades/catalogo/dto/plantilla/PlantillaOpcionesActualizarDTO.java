package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PlantillaOpcionesActualizarDTO {
    @NotNull
    private Long plantillaId;

    // lista nueva de opciones (ids). Usar para reemplazar o reordenar.
    @Size(min = 0)
    private List<Long> opcionIds;
}