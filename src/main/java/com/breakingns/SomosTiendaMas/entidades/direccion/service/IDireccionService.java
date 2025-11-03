package com.breakingns.SomosTiendaMas.entidades.direccion.service;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;

import java.util.List;

public interface IDireccionService {
    DireccionResponseDTO registrarDireccion(RegistroDireccionDTO dto);
    DireccionResponseDTO actualizarDireccion(Long id, ActualizarDireccionDTO dto);
    DireccionResponseDTO obtenerDireccion(Long id);
    List<DireccionResponseDTO> listarDireccionesPorUsuario(Long perfilUsuarioId);
    List<DireccionResponseDTO> listarDireccionesPorPerfilEmpresa(Long perfilEmpresaId);
}
