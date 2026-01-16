package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;

public interface IVarianteFisicoService {
    PhysicalPropertiesDTO obtenerPorVarianteId(Long varianteId);
    PhysicalPropertiesDTO crearOActualizarPorVariante(Long varianteId, PhysicalPropertiesDTO dto);
    void eliminarPorVarianteId(Long varianteId);
}