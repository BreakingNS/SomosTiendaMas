package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TelefonoResponseDTO {
    private Long id;
    private String ownerType; // USUARIO | EMPRESA
    private Long ownerId;

    private String tipo;
    private String numero;
    private String caracteristica;

    private Boolean activo;
    private Boolean verificado;
    private Boolean favorito; // null para empresa
}
