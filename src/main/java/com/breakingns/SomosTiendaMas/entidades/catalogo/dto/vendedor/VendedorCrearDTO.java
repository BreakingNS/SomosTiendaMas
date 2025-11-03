package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendedorCrearDTO {
    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    // opcional al crear
    private Double rating;
}