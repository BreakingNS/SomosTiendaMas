package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import java.time.LocalDate;

import com.breakingns.SomosTiendaMas.validation.MinAge;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroUsuarioDTO {
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Pattern(regexp = "^[A-Za-z0-9._-]{8,128}$", message = "El nombre de usuario solo puede contener letras, números, guion bajo, guion medio y punto, y tener entre 8 y 128 caracteres")
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
    
    // tipoUsuario ahora obligatorio: PERSONA_FISICA o EMPRESA
    @NotBlank(message = "El tipo de usuario es obligatorio")
    @Pattern(regexp = "^(PERSONA_FISICA|EMPRESA)$", message = "Tipo de usuario inválido")
    private String tipoUsuario;

    // Asegurar que el usuario acepte políticas y términos (no nulos y true)
    @NotNull(message = "Debe aceptar la política de privacidad")
    @AssertTrue(message = "Debe aceptar la política de privacidad")
    private Boolean aceptaPoliticaPriv;

    @NotNull(message = "Debe aceptar los términos y condiciones")
    @AssertTrue(message = "Debe aceptar los términos y condiciones")
    private Boolean aceptaTerminos;
    
    // Fecha de nacimiento del responsable
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @MinAge(value = 18, message = "Debe ser mayor de 18 años")
    private LocalDate fechaNacimientoResponsable;

    // Nuevo campo: género del responsable
    @NotNull(message = "El género del responsable es obligatorio")
    private String generoResponsable; // MASCULINO, FEMENINO, OTRO

    // Hacer obligatorios idioma, timezone y rol (según tu requerimiento)
    @NotBlank(message = "El idioma es obligatorio")
    private String idioma;

    @NotBlank(message = "La zona horaria es obligatoria")
    private String timezone;
    
    // Permite especificar el rol al registrar usuario (obligatorio según tu indicación)
    @NotBlank(message = "El rol es obligatorio")
    private String rol;
    
    public RegistroUsuarioDTO() {}

    // keep explicit rol accessors if quieres (Lombok los genera, estos son opcionales)
    public String getRol() {
        return rol;
    }
    public void setRol(String rol) {
        this.rol = rol;
    }
}
