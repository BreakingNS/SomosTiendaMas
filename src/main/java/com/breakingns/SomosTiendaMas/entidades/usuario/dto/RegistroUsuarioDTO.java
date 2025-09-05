package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroUsuarioDTO {
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Pattern(regexp = "^[A-Za-z0-9._-]{6,16}$", message = "El nombre de usuario solo puede contener letras, números, guion bajo, guion medio y punto, y tener entre 6 y 16 caracteres")
    private String username;

    @Email(message = "El correo electrónico no tiene un formato válido")
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, max = 16, message = "La contraseña debe tener entre 6 y 16 caracteres")
    @Pattern(regexp = "^\\S+$", message = "La contraseña no debe contener espacios")
    private String password;
    
    @NotBlank(message = "El nombre del responsable no puede estar vacío")
    private String nombreResponsable;
    
    @NotBlank(message = "El apellido del responsable no puede estar vacío")
    private String apellidoResponsable;

    @NotBlank(message = "El documento del responsable no puede estar vacío")
    private String documentoResponsable;
    
    @NotBlank(message = "El tipo de usuario es obligatorio")
    private String tipoUsuario; // PERSONA_FISICA, EMPRESA

    @NotNull(message = "Debe aceptar los términos y condiciones")
    private Boolean aceptaTerminos;

    @NotNull(message = "Debe aceptar la política de privacidad")
    private Boolean aceptaPoliticaPriv;
    
    // Nuevo campo: fecha de nacimiento del responsable
    private LocalDate fechaNacimientoResponsable;

    // Nuevo campo: género del responsable
    private String generoResponsable; // MASCULINO, FEMENINO, OTRO

    //ELIMINADO TELEFONO Y DIRECCION <----------------------------------------------
    private String idioma;
    private String timezone;
    
    // Permite especificar el rol al registrar usuario
    private String rol;
    
    public RegistroUsuarioDTO() {}
        public String getRol() {
            return rol;
        }
        public void setRol(String rol) {
            this.rol = rol;
        }
}
