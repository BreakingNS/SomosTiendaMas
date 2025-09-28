package com.breakingns.SomosTiendaMas.entidades.empresa.service;

import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.PerfilEmpresaResponseDTO;

public interface IPerfilEmpresaService {
    PerfilEmpresaResponseDTO registrarPerfilEmpresa(RegistroPerfilEmpresaDTO dto);
    PerfilEmpresaResponseDTO actualizarPerfilEmpresa(Long id, ActualizarPerfilEmpresaDTO dto);
    PerfilEmpresaResponseDTO obtenerPerfilEmpresa(Long id);

    // añadidos para concordar con el controller
    List<PerfilEmpresaResponseDTO> listarPerfiles();
    PerfilEmpresaResponseDTO actualizarPerfilEmpresaParcial(Long id, ActualizarPerfilEmpresaDTO dto);
    void eliminarPerfilEmpresa(Long id);
}
