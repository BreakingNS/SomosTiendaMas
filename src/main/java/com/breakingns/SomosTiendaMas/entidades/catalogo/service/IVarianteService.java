package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;

public interface IVarianteService {
    VarianteDTO crearVariante(VarianteCrearDTO dto);
    VarianteDTO obtenerPorId(Long id);
    VarianteDTO obtenerDefaultByProductoId(Long productoId);
    java.util.List<VarianteDTO> listarPorProductoId(Long productoId);
    java.util.List<VarianteDTO> listarTodas();
    void eliminarPermanente(Long id);
    VarianteDTO actualizar(Long id, VarianteCrearDTO dto);
    void eliminar(Long id);
    java.util.List<VarianteDTO> crearVariantesBatch(Long productoId, java.util.List<VarianteCrearDTO> dtos);
}
