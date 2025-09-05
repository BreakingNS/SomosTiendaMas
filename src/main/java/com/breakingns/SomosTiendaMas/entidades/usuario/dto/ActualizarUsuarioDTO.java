package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import java.time.LocalDate;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario.Genero;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ActualizarUsuarioDTO {
    private String username;
    private String password;
    private String email;

    private String documentoResponsable;
    private String nombreResponsable;
    private String apellidoResponsable;
    private Genero generoResponsable;
    private LocalDate fechaNacimientoResponsable;
    
    private String idioma;
    private String timezone;
}
