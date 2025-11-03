package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String username;
    private String email;
    private Boolean activo;
    private Boolean emailVerificado;
    private String tipoUsuario;
    private String telefonoPrincipal;
    private String direccionPrincipal;
    private String idioma;
    private String timezone;
}
