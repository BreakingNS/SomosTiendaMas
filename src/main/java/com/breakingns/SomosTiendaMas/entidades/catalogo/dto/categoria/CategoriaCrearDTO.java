package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.validation.UniqueSlug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriaCrearDTO {
    @NotBlank
    @Size(max = 160)
    private String nombre;

    @NotBlank
    @Size(max = 180)
    @UniqueSlug(entity = Categoria.class, message = "El slug de categoría ya existe")
    private String slug;

    private String descripcion;
    private Long categoriaPadreId;
}