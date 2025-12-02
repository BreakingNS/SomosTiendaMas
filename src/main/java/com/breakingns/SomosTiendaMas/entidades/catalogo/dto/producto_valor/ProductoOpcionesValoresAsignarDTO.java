package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_valor;

import java.util.List;

public class ProductoOpcionesValoresAsignarDTO {
    public Long productoId;
    public List<OpcionConValores> opciones;

    public static class OpcionConValores {
        public Long opcionId;
        public String nombre; // opcional, solo para conveniencia del cliente
        public Integer orden; // opcional
        public List<ValorDTO> valores;
    }

    public static class ValorDTO {
        public Long id;         // id del OpcionValor en BD
        public Long opcionId;   // redundante, pero útil para validación
        public String valor;
        public Integer orden;
    }
}