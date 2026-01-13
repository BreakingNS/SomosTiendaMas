package com.breakingns.SomosTiendaMas.entidades.catalogo.converter;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoProducto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el enum {@link EstadoProducto} y su representación en la base de datos (String).
 * - Persistencia: guarda el nombre del enum en mayúsculas.
 * - Lectura: acepta variantes case-insensitive y devuelve un valor por defecto si no se reconoce.
 */
@Converter(autoApply = false)
public class EstadoProductoConverter implements AttributeConverter<EstadoProducto, String> {

    @Override
    public String convertToDatabaseColumn(EstadoProducto attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public EstadoProducto convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim();
        for (EstadoProducto e : EstadoProducto.values()) {
            if (e.name().equalsIgnoreCase(v)) return e;
        }
        return EstadoProducto.BORRADOR;
    }
}
