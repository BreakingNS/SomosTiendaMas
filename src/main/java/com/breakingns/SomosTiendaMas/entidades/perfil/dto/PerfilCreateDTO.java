package com.breakingns.SomosTiendaMas.entidades.perfil.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionCreateDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilCreateDTO {
    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    private String tipoDocumento;
    private String documento;
    private LocalDate fechaNacimiento;
    private String genero;
    private String nacionalidad;

    // listas de DTOs para creación anidada (opcional)
    private List<TelefonoCreateDTO> telefonos;
    private List<DireccionCreateDTO> direcciones;
}
