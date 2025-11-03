package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OpcionCrearDTO {
    // ahora opcional para plantillas (producto null)
    private Long productoId;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    private Integer orden;
}