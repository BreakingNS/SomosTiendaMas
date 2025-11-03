package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.PerfilEmpresaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class ConsultaEmpresaCompletoDTO {
    private UsuarioResponseDTO responsable;
    private PerfilEmpresaResponseDTO perfilEmpresa;
    private List<DireccionResponseDTO> direccionesResponsable;
    private List<TelefonoResponseDTO> telefonosResponsable;
    private List<DireccionResponseDTO> direccionesEmpresa;
    private List<TelefonoResponseDTO> telefonosEmpresa;
}
