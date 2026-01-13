package com.breakingns.SomosTiendaMas.entidades.catalogo.converter;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoModeracion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el enum {@link EstadoModeracion} y su representación en la base de datos (String).
 * - Persistencia: guarda los valores en mayúsculas tal como espera la BD.
 * - Lectura: acepta variantes case-insensitive y algunas formas comunes (p.ej. "APROBADO").
 */
@Converter(autoApply = false)
public class EstadoModeracionConverter implements AttributeConverter<EstadoModeracion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoModeracion attribute) {
        if (attribute == null) return null;
        // Guardar en la BD con la forma esperada (mayúsculas)
        switch (attribute) {
            case PENDIENTE: return "PENDIENTE";
            case APROBADA:  return "APROBADA";
            case RECHAZADA: return "RECHAZADA";
            default: return attribute.name();
        }
    }

    @Override
    public EstadoModeracion convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim();
        // Aceptar variantes comunes ya existentes en la BD (p.ej. "APROBADO")
        if (v.equalsIgnoreCase("APROBADO")) return EstadoModeracion.APROBADA;
        // Comparación case-insensitive con los nombres del enum
        for (EstadoModeracion e : EstadoModeracion.values()) {
            if (e.name().equalsIgnoreCase(v)) return e;
        }
        // Si no se reconoce, devolver PENDIENTE por seguridad
        return EstadoModeracion.PENDIENTE;
    }
}