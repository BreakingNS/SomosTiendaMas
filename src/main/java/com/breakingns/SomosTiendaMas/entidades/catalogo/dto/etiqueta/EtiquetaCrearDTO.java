package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.validation.UniqueSlug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EtiquetaCrearDTO {
    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Size(max = 160)
    @UniqueSlug(entity = Etiqueta.class, message = "El slug de etiqueta ya existe")
    private String slug;
}