package com.breakingns.SomosTiendaMas.entidades.empresa.service;

import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.PerfilEmpresaResponseDTO;

public interface IPerfilEmpresaService {
    PerfilEmpresaResponseDTO registrarPerfilEmpresa(RegistroPerfilEmpresaDTO dto);
    PerfilEmpresaResponseDTO actualizarPerfilEmpresa(Long id, ActualizarPerfilEmpresaDTO dto);
    PerfilEmpresaResponseDTO obtenerPerfilEmpresa(Long id);
    // Puedes agregar más métodos según la lógica de negocio
}
