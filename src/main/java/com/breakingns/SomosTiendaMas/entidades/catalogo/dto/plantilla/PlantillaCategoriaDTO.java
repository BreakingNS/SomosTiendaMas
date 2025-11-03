package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaCategoriaDTO {
    private Long id;
    private Long categoriaId;
    private List<PlantillaCampoDTO> campos;
}
