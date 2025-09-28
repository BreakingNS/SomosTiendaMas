package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import java.time.LocalDate;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario.Genero;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarUsuarioDTO {

    private String username;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Email(message = "Email inválido")
    private String email;

    private String documentoResponsable;
    private String nombreResponsable;
    private String apellidoResponsable;
    private Genero generoResponsable;
    private LocalDate fechaNacimientoResponsable;
    
    private String idioma;
    private String timezone;

    // Normalizar entradas para que las anotaciones validen sobre los valores "trim"
    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setDocumentoResponsable(String documentoResponsable) {
        this.documentoResponsable = documentoResponsable == null ? null : documentoResponsable.trim();
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable == null ? null : nombreResponsable.trim();
    }

    public void setApellidoResponsable(String apellidoResponsable) {
        this.apellidoResponsable = apellidoResponsable == null ? null : apellidoResponsable.trim();
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma == null ? null : idioma.trim();
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone == null ? null : timezone.trim();
    }
}
