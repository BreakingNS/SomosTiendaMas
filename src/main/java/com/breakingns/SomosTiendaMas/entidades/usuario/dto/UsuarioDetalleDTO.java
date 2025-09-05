package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioDetalleDTO {
    private Long idUsuario;
    private String username;
    private String email;
    private Boolean activo;
    private Boolean emailVerificado;
    private String tipoUsuario;
    private String responsableNombre;
    private String responsableApellido;
    private String responsableDocumento;
    private String telefonoPrincipal;
    private String direccionPrincipal;
    private String idioma;
    private String timezone;
    private Integer intentosFallidosLogin;
    private Boolean cuentaBloqueada;
    private Boolean aceptaTerminos;
    private Boolean aceptaPoliticaPriv;
    private Boolean recibirPromociones;
    private Boolean recibirNewsletters;
    private Boolean notificacionesEmail;
    private Boolean notificacionesSms;
}
