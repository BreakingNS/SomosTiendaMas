package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.*;
import java.util.List;

public interface IVendedorService {
    VendedorResponseDTO crear(VendedorCrearDTO dto);
    VendedorResponseDTO actualizar(Long id, VendedorActualizarDTO dto);
    VendedorResponseDTO obtenerPorId(Long id);
    VendedorResponseDTO obtenerPorUserId(Long userId);
    List<VendedorResumenDTO> listarActivos();
    List<VendedorResumenDTO> buscarPorFiltro(VendedorFiltroDTO filtro);
    void eliminar(Long id); // soft-delete
    void eliminarPorUserId(Long userId); // borrado físico por userId
}