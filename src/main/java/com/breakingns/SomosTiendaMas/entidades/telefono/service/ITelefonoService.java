package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.*;

import java.util.List;

public interface ITelefonoService {
    TelefonoResponseDTO registrarTelefono(RegistroTelefonoDTO dto);
    TelefonoResponseDTO actualizarTelefono(Long id, ActualizarTelefonoDTO dto);
    TelefonoResponseDTO obtenerTelefono(Long id);
    List<TelefonoResponseDTO> listarTelefonosPorUsuario(Long perfilUsuarioId);
    List<TelefonoResponseDTO> listarTelefonosPorPerfilEmpresa(Long perfilEmpresaId);
}
