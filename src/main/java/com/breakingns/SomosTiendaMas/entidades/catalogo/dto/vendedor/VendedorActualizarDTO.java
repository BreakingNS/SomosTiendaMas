package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendedorActualizarDTO {
    @Size(max = 200)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    private Double rating;
}