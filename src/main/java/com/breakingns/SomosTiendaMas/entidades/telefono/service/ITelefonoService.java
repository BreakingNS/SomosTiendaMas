package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import java.util.List;

public interface ITelefonoService {
    TelefonoResponseDTO registrarTelefono(RegistroTelefonoDTO dto);
    TelefonoResponseDTO actualizarTelefono(Long id, ActualizarTelefonoDTO dto);
    TelefonoResponseDTO obtenerTelefono(Long id);
    List<TelefonoResponseDTO> listarTelefonosPorUsuario(Long idUsuario);
    List<TelefonoResponseDTO> listarTelefonosPorPerfilEmpresa(Long idPerfilEmpresa);
}
