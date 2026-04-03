package com.breakingns.SomosTiendaMas.entidades.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {
    private Long id;

    private String username;

    @Email
    private String email;

    @Size(min = 8, max = 128)
    private String password;

    private Boolean activo;

    private Integer version; // para optimistic locking
}
