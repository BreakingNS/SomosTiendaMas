package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullDTO;

/**
 * Servicio centralizado para crear producto + variantes + sub-recursos (precios, inventarios, imágenes, physical).
 * La operación es atómica: si alguno de los pasos falla la transacción se deshace.
 */
public interface IProductoCentralizadoService {
    /**
     * Crea el producto y sus sub-recursos; usa 'system' como usuario por defecto.
     */
    ProductoCentralizadoResponseFullDTO crear(ProductoCentralizadoCrearDTO dto);

    /**
     * Crea el producto y sus sub-recursos registrando la identidad del usuario que realiza la acción.
     */
    ProductoCentralizadoResponseFullDTO crear(ProductoCentralizadoCrearDTO dto, String usuario);

    /**
     * Obtiene el producto con todas sus variantes y sub-recursos en formato completo (mismo que crear).
     */
    ProductoCentralizadoResponseFullDTO obtenerPorId(Long productoId);
}
