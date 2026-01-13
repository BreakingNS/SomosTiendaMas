package com.breakingns.SomosTiendaMas.entidades.catalogo.converter;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.CondicionProducto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el enum {@link CondicionProducto} y su representación en la base de datos (String).
 * - Persistencia: guarda el nombre del enum en mayúsculas.
 * - Lectura: acepta variantes case-insensitive y devuelve un valor por defecto si no se reconoce.
 */
@Converter(autoApply = false)
public class CondicionProductoConverter implements AttributeConverter<CondicionProducto, String> {

    @Override
    public String convertToDatabaseColumn(CondicionProducto attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public CondicionProducto convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim();
        for (CondicionProducto e : CondicionProducto.values()) {
            if (e.name().equalsIgnoreCase(v)) return e;
        }
        return CondicionProducto.NUEVO;
    }
}
