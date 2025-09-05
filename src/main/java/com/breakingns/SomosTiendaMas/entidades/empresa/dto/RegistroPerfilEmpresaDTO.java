package com.breakingns.SomosTiendaMas.entidades.empresa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroPerfilEmpresaDTO {
    private Long idUsuario;
    private String razonSocial;
    private String cuit;
    private String condicionIVA;
    private String emailEmpresa;
    private String telefonoEmpresa;
    private String direccionFiscal;
    private Boolean requiereFacturacion;
}
