package com.breakingns.SomosTiendaMas.entidades.perfil.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUpdateDTO {
    private Long id;

    private String nombre;
    private String apellido;
    private String tipoDocumento;
    private String documento;
    private LocalDate fechaNacimiento;
    private String genero;
    private String nacionalidad;

    private Integer version;
}
