package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.Moneda;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoPrecio;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrecioVarianteResponseDTO {
    private Long id;
    private Long varianteId;
    private Long montoCentavos;
    private Moneda moneda;
    private TipoPrecio tipo;
    private boolean activo;
}
