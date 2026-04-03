package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ActualizarDireccionDTO {
    private String tipo;

    private Long paisId;
    private Long provinciaId;
    private Long departamentoId;
    private Long localidadId;
    private Long municipioId;

    private String calle;
    private String numero;
    private String piso;
    private String departamentoInterno;
    private String codigoPostal;
    private String referencia;

    private Boolean activa;
    private Boolean esPrincipal;

    // Metadatos opcionales para update
    private Long copiadaDeDireccionId;
    private Long canonicalAddressId;
    private String origen;
    private String originOwnerType;
    private Long originOwnerId;
    private Boolean syncEnabled;

    private String notas;

    private Integer version;
}
