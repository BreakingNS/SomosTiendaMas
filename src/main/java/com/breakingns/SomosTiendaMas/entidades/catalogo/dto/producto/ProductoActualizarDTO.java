package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoActualizarDTO {
    @Size(max = 200)
    private String nombre;

    @Size(max = 220)
    private String slug; // Validación de unicidad puede hacerse en servicio si cambia

    @Size(max = 10000)
    private String descripcion;

    // campos nuevos
    @Size(max = 5000)
    private String garantia;

    @Size(max = 5000)
    private String politicaDevoluciones;

    private Long marcaId;
    private Long categoriaId;

    private String metadataJson;

    // nuevo opcional
    private CondicionProducto condicion;
}
