package com.breakingns.SomosTiendaMas.entidades.empresa.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter @Setter
public class ActualizarPerfilEmpresaDTO {
    private String razonSocial;
    private String condicionIVA;
    @Email(message = "Email empresa inválido")
    private String emailEmpresa;
    private String telefonoEmpresa;
    private String direccionFiscal;
    private Boolean requiereFacturacion;
    private String categoriaEmpresa;
    private String sitioWeb;
    private String descripcionEmpresa;
    private String logoUrl;
    private String colorCorporativo;
    private String descripcionCorta;
    private String horarioAtencion;
    private String diasLaborales;
    private Integer tiempoProcesamientoPedidos;

    // si permiten actualizar CUIT, validar formato opcional:
    @Pattern(regexp = "^[0-9]{11}$", message = "CUIT inválido (11 dígitos)")
    private String cuit;
    @Size(max = 255, message = "Sitio web demasiado largo")
    private String sitioWebSafe;

    // Opcional: permitir activar/desactivar el perfil desde la API (admin)
    private Boolean activo;
}
