package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ConsultaUsuarioCompletoDTO {
    private UsuarioResponseDTO usuario;
    private List<DireccionResponseDTO> direcciones;
    private List<TelefonoResponseDTO> telefonos;
}
