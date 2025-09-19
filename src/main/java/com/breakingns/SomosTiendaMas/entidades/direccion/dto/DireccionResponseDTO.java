package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DireccionResponseDTO {
    private Long idDireccion;
    private String tipo;
    private String calle;
    private String numero;
    private String piso;
    private String referencia;
    private Boolean activa;
    private Boolean esPrincipal;
    private String codigoPostal;
    //private Boolean usarComoEnvio;

    // Agrega estos campos y sus setters/getters
    private Long idPais;
    private Long idProvincia;
    private Long idDepartamento;
    private Long idLocalidad;
    private Long idMunicipio;
}
