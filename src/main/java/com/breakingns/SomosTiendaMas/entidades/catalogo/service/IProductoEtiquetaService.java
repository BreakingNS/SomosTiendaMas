package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.etiqueta.EtiquetaResumenDTO;

import java.util.List;

public interface IProductoEtiquetaService {
    EtiquetaResumenDTO asignarEtiqueta(Long productoId, Long etiquetaId);
    void quitarEtiqueta(Long productoId, Long etiquetaId);
    List<EtiquetaResumenDTO> listarEtiquetasPorProductoId(Long productoId);
    List<Long> listarProductoIdsPorEtiquetaId(Long etiquetaId);
    boolean existeRelacion(Long productoId, Long etiquetaId);
    void eliminarRelacion(Long id); // soft-delete por id de relación
} 
