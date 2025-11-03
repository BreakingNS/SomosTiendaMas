package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared;

import lombok.Data;

import java.util.Map;

@Data
public class ApiErrorDTO {
    private String code;
    private String message;
    private Map<String, String> fieldErrors; // field -> message
    private String details;
}