package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ActualizarTelefonoDTO {
    private String tipo;
    private String numero;
    private String caracteristica;
    private Boolean activo;
    private Boolean verificado;
    private Long esCopiaDe;
}
