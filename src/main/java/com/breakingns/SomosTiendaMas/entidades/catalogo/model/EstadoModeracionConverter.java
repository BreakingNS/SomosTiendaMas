package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.EstadoModeracion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EstadoModeracionConverter implements AttributeConverter<EstadoModeracion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoModeracion attribute) {
        if (attribute == null) return null;
        // Guardar exactamente como espera la constraint de la BD (mayúsculas)
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
        // Aceptar variantes comunes que ya se vieron en inserts (p.ej. "APROBADO")
        if (v.equalsIgnoreCase("APROBADO")) return EstadoModeracion.APROBADA;
        // Match case-insensitive con los nombres del enum
        for (EstadoModeracion e : EstadoModeracion.values()) {
            if (e.name().equalsIgnoreCase(v)) return e;
        }
        // Si no se reconoce, devolver PENDIENTE por seguridad (o null según prefieras)
        return EstadoModeracion.PENDIENTE;
    }
}