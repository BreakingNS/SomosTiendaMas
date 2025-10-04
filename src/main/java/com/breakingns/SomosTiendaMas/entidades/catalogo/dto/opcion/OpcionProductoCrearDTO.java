package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OpcionProductoCrearDTO {
    @NotNull
    private Long productoId;

    @NotBlank
    private String nombre;

    private Integer orden;
}
