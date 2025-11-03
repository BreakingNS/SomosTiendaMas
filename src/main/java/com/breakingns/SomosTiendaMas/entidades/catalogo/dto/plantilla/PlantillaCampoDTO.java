package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CampoTipo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaCampoDTO {
    private Long id;
    private String nombre;
    private String slug;
    private CampoTipo tipo;
    private String opcionesJson;
    private Integer orden;
    private boolean requerido;
}
