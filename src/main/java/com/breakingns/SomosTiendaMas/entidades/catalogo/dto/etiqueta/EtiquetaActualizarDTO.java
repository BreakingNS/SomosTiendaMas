package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta;

import lombok.Data;

import jakarta.validation.constraints.Size;

@Data
public class EtiquetaActualizarDTO {
    @Size(max = 120)
    private String nombre;

    @Size(max = 160)
    private String slug;
}