package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter @Setter
public class RegistroDireccionDTO {

    // NOTE: validar en el service que uno de estos (idUsuario o idPerfilEmpresa) esté presente según el contexto
    private Long idUsuario; // Obligatorio si es para usuario
    private Long idPerfilEmpresa; // Obligatorio si es para empresa, sino null

    @NotBlank(message = "tipo es obligatorio")
    @Pattern(regexp = "PERSONAL|FISCAL|ENVIO|FACTURACION|EMPRESA", message = "tipo de dirección inválido")
    private String tipo; // PERSONAL, FISCAL, ENVIO, FACTURACION, EMPRESA

    @NotBlank(message = "calle es obligatoria")
    @Size(max = 255)
    private String calle; // no puede ser null

    @NotBlank(message = "numero es obligatorio")
    @Size(max = 50)
    private String numero; // no puede ser null

    private String piso; // puede ser null
    private String referencia; // puede ser null

    @NotNull(message = "activa es obligatoria")
    private Boolean activa; // no puede ser null

    @NotNull(message = "esPrincipal es obligatorio")
    private Boolean esPrincipal; // no puede ser null
    /*
    @NotNull(message = "usarComoEnvio es obligatorio")
    private Boolean usarComoEnvio; // no puede ser null
    */

    // IDs de entidades de ubicación (todos obligatorios)
    @NotNull(message = "idPais es obligatorio")
    private Long idPais;

    @NotNull(message = "idProvincia es obligatorio")
    private Long idProvincia;

    @NotNull(message = "idDepartamento es obligatorio")
    private Long idDepartamento;

    @NotNull(message = "idLocalidad es obligatorio")
    private Long idLocalidad;

    @NotNull(message = "idMunicipio es obligatorio")
    private Long idMunicipio;

    @NotBlank(message = "codigoPostal es obligatorio")
    @Size(max = 20)
    private String codigoPostal; // no puede ser null
}
