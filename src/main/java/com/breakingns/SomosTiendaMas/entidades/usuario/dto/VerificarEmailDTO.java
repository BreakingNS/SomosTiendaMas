package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VerificarEmailDTO {
    private String email;
    private String codigoVerificacion;
}
