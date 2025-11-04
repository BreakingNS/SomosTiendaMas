package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion;

import java.util.List;

public class ProductoOpcionesAsignarDTO {
    public Long productoId;
    public List<OpcionSeleccionada> opciones;

    public static class OpcionSeleccionada {
        public Long opcionId;
        public List<Long> opcionValorIds; // vacío/null si no aplica (boolean/text)
        public Boolean requerido;
        public Boolean activo;
        public Integer orden; // opcional: si no viene se calcula
    }
}