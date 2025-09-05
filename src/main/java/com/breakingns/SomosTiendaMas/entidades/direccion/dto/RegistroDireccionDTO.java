package com.breakingns.SomosTiendaMas.entidades.direccion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroDireccionDTO {
    private Long idUsuario;
    private Long idPerfilEmpresa;
    private String tipo;
    private String calle;
    private String numero;
    private String piso;
    private String departamento;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String pais;
    private String referencia;
    private Boolean activa;
    private Boolean esPrincipal;
    private Long esCopiaDe;
}
