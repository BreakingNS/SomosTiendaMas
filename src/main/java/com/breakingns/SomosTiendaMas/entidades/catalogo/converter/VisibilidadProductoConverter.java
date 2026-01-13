package com.breakingns.SomosTiendaMas.entidades.catalogo.converter;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.VisibilidadProducto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el enum {@link VisibilidadProducto} y su representación en la base de datos (String).
 * - Persistencia: guarda el nombre del enum en mayúsculas.
 * - Lectura: acepta variantes case-insensitive y devuelve un valor por defecto si no se reconoce.
 */
@Converter(autoApply = false)
public class VisibilidadProductoConverter implements AttributeConverter<VisibilidadProducto, String> {

    @Override
    public String convertToDatabaseColumn(VisibilidadProducto attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public VisibilidadProducto convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim();
        for (VisibilidadProducto e : VisibilidadProducto.values()) {
            if (e.name().equalsIgnoreCase(v)) return e;
        }
        return VisibilidadProducto.PUBLICO;
    }
}
