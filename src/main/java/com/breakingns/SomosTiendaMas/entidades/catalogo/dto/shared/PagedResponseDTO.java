package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared;

import lombok.Data;

import java.util.List;

@Data
public class PagedResponseDTO<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;
}