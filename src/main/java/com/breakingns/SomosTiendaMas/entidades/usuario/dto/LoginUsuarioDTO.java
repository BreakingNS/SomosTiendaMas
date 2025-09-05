package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginUsuarioDTO {
    @NotBlank(message = "El usuario o email no puede estar vacío")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    public LoginUsuarioDTO() {}
    public LoginUsuarioDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
}
