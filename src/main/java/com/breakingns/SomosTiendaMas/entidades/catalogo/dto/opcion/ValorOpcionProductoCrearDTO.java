package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ValorOpcionProductoCrearDTO {
    @NotBlank
    @Size(max = 120)
    private String valor;

    @Size(max = 160)
    private String slug;

    private Integer orden = 0;
}
