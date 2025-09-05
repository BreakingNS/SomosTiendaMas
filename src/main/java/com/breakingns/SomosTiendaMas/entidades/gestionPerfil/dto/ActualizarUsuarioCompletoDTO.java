package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.ActualizarUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ActualizarUsuarioCompletoDTO {
    private ActualizarUsuarioDTO usuario;
    private List<ActualizarDireccionDTO> direcciones;
    private List<ActualizarTelefonoDTO> telefonos;
}
