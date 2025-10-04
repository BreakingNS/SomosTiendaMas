package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class VarianteValoresAsignarDTO {
    @NotEmpty
    private List<Long> valorIds;
}
