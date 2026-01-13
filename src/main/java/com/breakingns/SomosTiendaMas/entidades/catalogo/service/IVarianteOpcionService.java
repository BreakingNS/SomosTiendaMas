package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.*;

import java.util.List;

public interface IVarianteOpcionService {
    void asignarOpciones(VarianteOpcionesAsignarDTO dto, String usuario);

    VarianteConOpcionesDTO obtenerVarianteConOpciones(Long varianteId);

    List<VarianteConOpcionesDTO> obtenerTodosConOpciones();

    void modificarOpciones(Long varianteId, VarianteOpcionesAsignarDTO dto, String usuario);
    // devuelve por variante las opciones con sus valores (variante_valor si existen, sino valores plantilla)
    VarianteConOpcionesValoresDTO obtenerVarianteConOpcionesConValores(Long varianteId);

    // idem para todos los variantes
    List<VarianteConOpcionesValoresDTO> obtenerTodosConOpcionesConValores();
}