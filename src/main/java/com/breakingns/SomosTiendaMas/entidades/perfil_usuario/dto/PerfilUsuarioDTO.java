package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class PerfilUsuarioDTO {

    @NotNull(message = "usuarioId es requerido cuando el payload indica la FK")
    private Long usuarioId;

    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellido;

    @Size(max = 20)
    private String documento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @Size(max = 16)
    private String genero;

    @Size(max = 100)
    private String cargo;

    @Size(max = 150)
    private String correoAlternativo;

    public PerfilUsuarioDTO() {
        this.cargo = "-";
        this.correoAlternativo = "-";
    }

    // getters y setters
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = (cargo == null || cargo.trim().isEmpty()) ? "-" : cargo.trim(); }

    public String getCorreoAlternativo() { return correoAlternativo; }
    public void setCorreoAlternativo(String correoAlternativo) { this.correoAlternativo = (correoAlternativo == null || correoAlternativo.trim().isEmpty()) ? "-" : correoAlternativo.trim(); }
}
