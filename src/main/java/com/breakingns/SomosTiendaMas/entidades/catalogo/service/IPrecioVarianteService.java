package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;

import java.util.Optional;

public interface IPrecioVarianteService {
    PrecioVariante setPrecioLista(Long varianteId, Long montoCentavos);
    Optional<PrecioVariante> obtenerPrecioVigente(Long varianteId);
}
