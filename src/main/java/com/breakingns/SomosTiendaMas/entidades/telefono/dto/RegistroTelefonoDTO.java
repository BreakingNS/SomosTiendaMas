package com.breakingns.SomosTiendaMas.entidades.telefono.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistroTelefonoDTO {
    // Uno de estos debe venir informado
    private Long perfilUsuarioId;
    private Long perfilEmpresaId;

    @NotBlank
    private String tipo; // PRINCIPAL | SECUNDARIO | WHATSAPP (usuario) | EMPRESA (empresa)

    @NotBlank
    private String numero;

    @NotBlank
    private String caracteristica; // Debe existir en tabla codigos_area
    
    private Boolean activo;      // default true
    private Boolean favorito;    // solo aplica a usuario (default false)
    private Boolean verificado;

    // Constructor con valores por defecto
    public RegistroTelefonoDTO() {
        this.activo = true;
        this.favorito = false;
        this.verificado = false;
    }

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