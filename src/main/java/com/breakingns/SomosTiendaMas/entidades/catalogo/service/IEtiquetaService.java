package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.*;
import java.util.List;

public interface IEtiquetaService {
    EtiquetaResponseDTO crear(EtiquetaCrearDTO dto);
    EtiquetaResponseDTO actualizar(Long id, EtiquetaActualizarDTO dto);
    EtiquetaResponseDTO obtenerPorId(Long id);
    EtiquetaResponseDTO obtenerPorSlug(String slug);
    List<EtiquetaResumenDTO> listarActivas();
    void eliminar(Long id); // soft-delete
}