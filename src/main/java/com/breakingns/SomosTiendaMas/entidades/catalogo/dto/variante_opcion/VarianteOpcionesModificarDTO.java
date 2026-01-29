package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion;

import java.util.List;

public class VarianteOpcionesModificarDTO {
    public Long varianteId;
    public Boolean asDefault; // opcional, también puede pasarse como query param
    public List<Op> opciones;

    public static class Op {
        public Long opcionId;
        public String action; // add|update|delete
        public String nombre;
        public Integer orden;
        public Boolean requerido;
        public Boolean activo;
        public List<Valor> valores;
    }

    public static class Valor {
        public Long id; // id de OpcionValor cuando aplique
        public String action; // add|update|delete
        public String valor; // texto/ representación
        public Integer orden;
    }
}
