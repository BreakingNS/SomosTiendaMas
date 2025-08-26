package com.breakingns.SomosTiendaMas.auth.dto.shared;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    // Getters y setters

    public RegistroUsuarioDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public RegistroUsuarioDTO() {
    }
}
