package com.breakingns.SomosTiendaMas.auth.dto.shared;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DepartamentoDTO {
    private Long id;
    private String nombre;

    public DepartamentoDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    // getters y setters
}
