package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.ActualizarUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ActualizarEmpresaCompletoDTO {
    private ActualizarUsuarioDTO responsable;
    private ActualizarPerfilEmpresaDTO perfilEmpresa;
    private List<ActualizarDireccionDTO> direccionesResponsable;
    private List<ActualizarTelefonoDTO> telefonosResponsable;
    private List<ActualizarDireccionDTO> direccionesEmpresa;
    private List<ActualizarTelefonoDTO> telefonosEmpresa;
}
