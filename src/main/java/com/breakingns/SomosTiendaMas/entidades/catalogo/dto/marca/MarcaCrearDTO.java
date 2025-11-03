package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.marca;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.validation.UniqueSlug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MarcaCrearDTO {
    @NotBlank
    @Size(max = 160)
    private String nombre;

    @NotBlank
    @Size(max = 180)
    @UniqueSlug(entity = Marca.class, message = "El slug de marca ya existe")
    private String slug;

    private String descripcion;

    // Opcional: si la marca se crea desde la UI por un vendedor/usuario
    private Boolean creadaPorUsuario;
    private Long creadaPorVendedorId;
}