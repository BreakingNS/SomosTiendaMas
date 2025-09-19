package com.breakingns.SomosTiendaMas.auth.dto.shared;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MunicipioDTO {
    private Long id;
    private String nombre;

    public MunicipioDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
    // getters y setters
}
