package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PlantillaCategoriaCrearDTO {
    @NotNull
    private Long categoriaId;

    @NotBlank
    @Size(max = 160)
    private String nombre;

    private String descripcion;

    // Opciones a asociar (ids de Opcion). Opcional en creación.
    private List<Long> opcionIds;
}