package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class RegistroUsuarioCompletoDTO {
    @Valid
    private RegistroUsuarioDTO usuario;
    @Valid
    private List<RegistroDireccionDTO> direcciones;
    @Valid
    private List<RegistroTelefonoDTO> telefonos;
}
