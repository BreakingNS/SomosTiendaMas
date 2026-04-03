package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelefonoResponseDTO {
    private Long id;

    // Owner: "USUARIO" | "EMPRESA"
    private String ownerType;
    private Long ownerId;

    private String tipo;
    private String numero;
    private String caracteristica;
    private String formato;

    private Boolean activo;
    private Boolean verificado;
    private Boolean favorito; // null para empresa cuando no aplique
}
