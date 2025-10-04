package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductoActualizarDTO {
    @Size(max = 200)
    private String nombre;

    @Size(max = 220)
    private String slug; // Validación de unicidad puede hacerse en servicio si cambia

    @Size(max = 65535)
    private String descripcion;

    private Long marcaId;
    private Long categoriaId;

    private String metadataJson;
}
