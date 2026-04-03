package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponseDTO {
    private Long id;

    // Owner: "USUARIO" o "EMPRESA"
    private String ownerType;
    private Long ownerId;

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
    private Long copiadaDeDireccionId;
    private Long canonicalAddressId;

    private String origen;
    private String originOwnerType;
    private Long originOwnerId;
    private Boolean syncEnabled;

    private String notas;

    private Integer version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
