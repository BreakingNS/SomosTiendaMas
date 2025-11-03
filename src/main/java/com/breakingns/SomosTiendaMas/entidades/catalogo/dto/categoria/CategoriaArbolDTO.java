package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.categoria;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoriaArbolDTO {
    private Long id;
    private String nombre;
    private String slug;
    private Long categoriaPadreId;
    private List<CategoriaArbolDTO> hijos = new ArrayList<>();
}