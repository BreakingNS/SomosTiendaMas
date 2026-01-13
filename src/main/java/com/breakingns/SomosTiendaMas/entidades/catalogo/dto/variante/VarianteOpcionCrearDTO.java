package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VarianteOpcionCrearDTO {
    // ahora opcional para plantillas (producto null)
    private Long productoId;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    private Integer orden;
}