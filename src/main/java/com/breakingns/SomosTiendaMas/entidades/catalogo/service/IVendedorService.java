package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.vendedor.*;
import java.util.List;

public interface IVendedorService {
    VendedorResponseDTO crear(VendedorCrearDTO dto);
    VendedorResponseDTO actualizar(Long id, VendedorActualizarDTO dto);
    VendedorResponseDTO obtenerPorId(Long id);
    VendedorResponseDTO obtenerPorUserId(Long userId);
    VendedorResponseDTO obtenerPorEmpresaId(Long empresaId);
    List<VendedorResumenDTO> listarActivos();
    List<VendedorResumenDTO> buscarPorFiltro(VendedorFiltroDTO filtro);
    void eliminar(Long id); // soft-delete
    void eliminarPorUserId(Long userId); // soft-delete por userId
    void purgarPorUserId(Long userId); // borrado físico (purge) por userId

    // Operaciones por empresaId
    VendedorResponseDTO actualizarPorEmpresaId(Long empresaId, VendedorActualizarDTO dto);
    void eliminarPorEmpresaId(Long empresaId); // soft-delete por empresaId
    void purgarPorEmpresaId(Long empresaId); // purge por empresaId

    // Operaciones por userId (actualizar)
    VendedorResponseDTO actualizarPorUserId(Long userId, VendedorActualizarDTO dto);
}