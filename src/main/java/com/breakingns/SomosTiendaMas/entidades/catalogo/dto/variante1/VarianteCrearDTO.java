package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para crear una variante. `productoId` puede omitirse si la creación se realiza
 * a través de la ruta anidada `/productos/{id}/variantes`.
 */
@Data
public class VarianteCrearDTO {
    private Long productoId;

    @Size(max = 120)
    private String sku; // parte específica de la variante

    // Atributos en JSON (opcional)
    private String attributesJson;

    // hash opcional (si se pre-calcula en cliente)
    @Size(max = 64)
    private String attributesHash;

    private Boolean esDefault = false;
    private Boolean activo = true;
}
