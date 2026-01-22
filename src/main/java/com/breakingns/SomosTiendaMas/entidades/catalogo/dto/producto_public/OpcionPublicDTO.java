package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_public;

import lombok.Data;
import java.util.List;

@Data
public class OpcionPublicDTO {
    private String nombre;
    private Integer orden;
    private List<ValorPublicDTO> valores;
}
