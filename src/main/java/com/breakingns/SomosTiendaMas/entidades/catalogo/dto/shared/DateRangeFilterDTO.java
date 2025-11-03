package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DateRangeFilterDTO {
    private LocalDateTime desde;
    private LocalDateTime hasta;
}