package com.breakingns.SomosTiendaMas.entidades.gestionPerfil.dto;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.RegistroUsuarioDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class RegistroEmpresaCompletoDTO {
    @Valid
    private RegistroUsuarioDTO responsable;
    @Valid
    private RegistroPerfilEmpresaDTO perfilEmpresa;
    @Valid
    private List<RegistroDireccionDTO> direccionesResponsable;
    @Valid
    private List<RegistroTelefonoDTO> telefonosResponsable;
    @Valid
    private List<RegistroDireccionDTO> direccionesEmpresa;
    @Valid
    private List<RegistroTelefonoDTO> telefonosEmpresa;
}
