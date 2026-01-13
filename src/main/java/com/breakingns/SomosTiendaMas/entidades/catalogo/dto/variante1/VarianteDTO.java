package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VarianteDTO {
    private Long id;
    private Long productoId;

    private String sku;
    private String skuResuelto; // producto.slug + "-" + variante.sku (convention)

    private String attributesJson;
    private String attributesHash;

    private Boolean esDefault;
    private Boolean activo;

    // campos resueltos para UI
    private Long precioResueltoCentavos;
    private Boolean precioResueltoActivo;
    private Long stockResuelto;

    private List<Long> imagenIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
