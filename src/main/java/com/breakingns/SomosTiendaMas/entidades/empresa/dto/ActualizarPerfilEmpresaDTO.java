package com.breakingns.SomosTiendaMas.entidades.empresa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ActualizarPerfilEmpresaDTO {
    private String razonSocial;
    private String condicionIVA;
    private String emailEmpresa;
    private String telefonoEmpresa;
    private String direccionFiscal;
    private Boolean requiereFacturacion;
    private String categoriaEmpresa;
    private String sitioWeb;
    private String descripcionEmpresa;
    private String logoUrl;
    private String colorCorporativo;
    private String descripcionCorta;
    private String horarioAtencion;
    private String diasLaborales;
    private Integer tiempoProcesamientoPedidos;
}
