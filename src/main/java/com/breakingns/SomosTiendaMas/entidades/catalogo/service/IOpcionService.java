package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import java.util.List;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;

public interface IOpcionService {
    OpcionResponseDTO crearOpcion(OpcionCrearDTO dto);
    OpcionResponseDTO actualizarOpcion(Long opcionId, OpcionActualizarDTO dto);
    OpcionResponseDTO obtenerOpcionPorId(Long opcionId);
    List<OpcionResumenDTO> listarOpcionesPorProductoId(Long productoId);
    void eliminarOpcion(Long opcionId);

    OpcionValorResponseDTO crearValor(OpcionValorCrearDTO dto);
    OpcionValorResponseDTO actualizarValor(Long valorId, OpcionValorActualizarDTO dto);
    OpcionValorResponseDTO obtenerValorPorId(Long valorId);
    List<OpcionValorResponseDTO> listarValoresPorOpcionId(Long opcionId);
    void eliminarValor(Long valorId);

    // nuevas operaciones para la relación Producto <-> Opción
    List<OpcionResumenDTO> listarPlantillas();
    OpcionResponseDTO asignarOpcionAProducto(Long productoId, Long opcionId);
    void desasignarOpcionDeProducto(Long productoId, Long opcionId);

    // devuelve todas las opciones (plantillas) con su lista de valores simplificada
    List<OpcionConValoresSimpleDTO> listarOpcionesConValores();
}