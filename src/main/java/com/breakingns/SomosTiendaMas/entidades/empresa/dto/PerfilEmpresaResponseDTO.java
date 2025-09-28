package com.breakingns.SomosTiendaMas.entidades.empresa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PerfilEmpresaResponseDTO {
    private Long idUsuario;
    private Long id;
    private String razonSocial;
    private String cuit;
    private String condicionIVA;
    private String estadoAprobado;
    private String emailEmpresa;
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
    private String fechaCreacion;
    private String fechaUltimaModificacion;

    // Nuevo: estado activo del perfil
    private Boolean activo;
}
