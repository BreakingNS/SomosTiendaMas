package com.breakingns.SomosTiendaMas.entidades.perfil_usuario.dto;

import java.time.LocalDate;

public class PerfilUsuarioResponseDTO {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String documento;
    private LocalDate fechaNacimiento;
    private String genero;
    private String cargo;
    private String correoAlternativo;

    public PerfilUsuarioResponseDTO() {}

    public PerfilUsuarioResponseDTO(Long id, Long usuarioId, String nombre, String apellido,
                                    String documento, LocalDate fechaNacimiento, String genero,
                                    String cargo, String correoAlternativo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.cargo = cargo;
        this.correoAlternativo = correoAlternativo;
    }

    // ...getters y setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getCorreoAlternativo() { return correoAlternativo; }
    public void setCorreoAlternativo(String correoAlternativo) { this.correoAlternativo = correoAlternativo; }
}
