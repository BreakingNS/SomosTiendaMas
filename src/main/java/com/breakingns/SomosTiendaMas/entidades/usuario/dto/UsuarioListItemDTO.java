package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioListItemDTO {
    private Long idUsuario;
    private String username;
    private String email;
    private Boolean activo;
    private String tipoUsuario;
}