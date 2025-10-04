package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VarianteValoresResponseDTO {
    private Long varianteId;
    private List<Long> valorIds;
}
