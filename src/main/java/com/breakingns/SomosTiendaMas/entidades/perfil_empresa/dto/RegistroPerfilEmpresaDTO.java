package com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter @Setter
public class RegistroPerfilEmpresaDTO {
    private Long idUsuario;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    // nombre para mostrar (opcional)
    @Size(max = 200)
    private String nombreComercial;

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "CUIT inválido (11 dígitos)")
    private String cuit;

    @NotBlank(message = "Condición IVA obligatoria")
    private String condicionIVA;

    @Email(message = "Email empresa inválido")
    @NotBlank(message = "Email de la empresa es obligatorio")
    private String emailEmpresa;

    @NotNull(message = "Indicar si requiere facturación")
    private Boolean requiereFacturacion;

    private Boolean activo;

    // --- Nuevos campos para relaciones / acceso ---

    /**
     * Id del PerfilUsuario que actuará como responsable de la empresa.
     * Opcional: si se crea el PerfilUsuario en el mismo flujo, puede quedar null
     * y asignarse desde el service después de crear el perfil.
     */
    private Long responsablePerfilId;

    /**
     * Datos de acceso del Usuario que dará acceso asociado a la empresa.
     * El flujo puede aceptar username o email (uno u otro) para buscar/vincular
     * el Usuario existente o crear uno nuevo si corresponde.
     */
    private String accesoUsername;
    private String accesoEmail;

    public RegistroPerfilEmpresaDTO() {
        // Valores por defecto según payload solicitado
        this.requiereFacturacion = true;
        this.activo = true;
    }
}
