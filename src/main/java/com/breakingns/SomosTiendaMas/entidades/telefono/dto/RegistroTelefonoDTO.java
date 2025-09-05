package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroTelefonoDTO {
    private Long idUsuario;
    private Long idPerfilEmpresa;
    private String tipo;
    private String numero;
    private String caracteristica;
    private Boolean activo;
    private Boolean verificado;
    private Long esCopiaDe;
}
