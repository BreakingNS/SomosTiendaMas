package com.breakingns.SomosTiendaMas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroUsuarioDTO {
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String username;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @Size(min = 6, max = 16, message = "La contraseña debe tener entre 6 y 16 caracteres")
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
