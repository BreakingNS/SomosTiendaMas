package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroTelefonoDTO {

    private Long idUsuario;
    private Long idPerfilEmpresa;

    @NotBlank(message = "El tipo de teléfono es obligatorio")
    private String tipo;

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "Número inválido (solo dígitos, 6-15)")
    private String numero;

    @NotBlank(message = "La característica es obligatoria")
    @Pattern(regexp = "^[0-9]{1,4}$", message = "Característica inválida (solo dígitos)")
    private String caracteristica;

    private Boolean activo;
    private Boolean verificado;

    // Normalizar entradas antes de la validación y persistencia.
    // Al definir setters personalizados, Lombok no generará los setters para esos campos,
    // pero seguirá generando getters y setters para los demás.
    public void setTipo(String tipo) {
        this.tipo = tipo == null ? null : tipo.trim();
    }

    public void setNumero(String numero) {
        this.numero = numero == null ? null : numero.trim();
    }

    public void setCaracteristica(String caracteristica) {
        this.caracteristica = caracteristica == null ? null : caracteristica.trim();
    }
}
