package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RegistroDireccionDTO {
    // Uno de estos debe estar presente: perfilUsuarioId o perfilEmpresaId
    private Long perfilUsuarioId;
    private Long perfilEmpresaId;

    @NotBlank
    private String tipo;

    @NotNull
    private Long paisId;
    @NotNull
    private Long provinciaId;
    @NotNull
    private Long departamentoId;
    @NotNull
    private Long localidadId;
    @NotNull
    private Long municipioId;

    @NotBlank
    private String calle;
    @NotBlank
    private String numero;
    private String piso;
    private String departamentoInterno;
    @NotBlank
    private String codigoPostal;
    private String referencia;
    @NotNull
    private Boolean activa = true;
    @NotNull
    private Boolean esPrincipal = true;

    // Metadatos opcionales de import/duplicado
    private Long copiadaDeDireccionId;
    private Long canonicalAddressId;
    private String origen; // MANUAL|IMPORT|API|COPIA
    private String originOwnerType; // USUARIO|EMPRESA
    private Long originOwnerId;
    private Boolean syncEnabled = false;

    private String notas;
}
