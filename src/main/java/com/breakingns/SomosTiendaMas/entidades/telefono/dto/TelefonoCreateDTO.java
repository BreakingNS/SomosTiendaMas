package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelefonoCreateDTO {
    private String tipoTelefono; // usar enum name()
    private String etiqueta;
    private Long codigoAreaId; // referencia por id
    private String caracteristica;

    @NotBlank
    private String numero;

    private Boolean favorito = false;
}
