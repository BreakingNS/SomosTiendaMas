package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginResponseDTO {
    private String token;
    private Long idUsuario;
    private String username;
    private String email;
}
