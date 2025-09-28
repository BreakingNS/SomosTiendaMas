package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
public class ActualizarDireccionDTO {

    // Tipo opcional pero si se envía debe ser uno válido
    @Pattern(regexp = "PERSONAL|FISCAL|ENVIO|FACTURACION|EMPRESA", message = "tipo de dirección inválido")
    private String tipo;

    // Campos de dirección (opcionales en update; si se envían se limitan en tamaño)
    @Size(max = 255, message = "calle demasiado larga")
    private String calle;

    @Size(max = 50, message = "numero demasiado largo")
    private String numero;

    @Size(max = 50, message = "piso demasiado largo")
    private String piso;

    @Size(max = 255, message = "referencia demasiado larga")
    private String referencia;

    private Boolean activa;
    private Boolean esPrincipal;

    @Size(max = 20, message = "codigoPostal demasiado largo")
    private String codigoPostal;

    // IDs de ubicación opcionales (si se envían se validan como positivos)
    @Positive(message = "idPais debe ser un número positivo")
    private Long idPais;

    @Positive(message = "idProvincia debe ser un número positivo")
    private Long idProvincia;

    @Positive(message = "idDepartamento debe ser un número positivo")
    private Long idDepartamento;

    @Positive(message = "idLocalidad debe ser un número positivo")
    private Long idLocalidad;

    @Positive(message = "idMunicipio debe ser un número positivo")
    private Long idMunicipio;
}
