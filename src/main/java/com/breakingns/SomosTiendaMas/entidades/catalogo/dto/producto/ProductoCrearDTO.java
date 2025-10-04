package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.validation.UniqueSlug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoCrearDTO {
    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotBlank
    @Size(max = 220)
    @UniqueSlug(entity = Producto.class, message = "El slug de producto ya existe")
    private String slug;

    @Size(max = 65535)
    private String descripcion;

    private Long marcaId;
    private Long categoriaId;

    private String metadataJson;
}
