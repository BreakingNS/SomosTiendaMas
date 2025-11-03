package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared;

import lombok.Data;

@Data
public class PageRequestDTO {
    private Integer page = 0;
    private Integer size = 20;
    private String sort; // ejemplo "nombre,asc" o "createdAt,desc"
}