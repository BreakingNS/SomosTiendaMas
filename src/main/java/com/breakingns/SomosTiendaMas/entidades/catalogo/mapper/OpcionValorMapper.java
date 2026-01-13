package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;

import java.util.List;
import java.util.stream.Collectors;

public final class OpcionValorMapper {

    private OpcionValorMapper() {}

    public static OpcionValorResponseDTO toResponse(OpcionValor v) {
        if (v == null) return null;
        return OpcionValorResponseDTO.builder()
                .id(v.getId())
                .opcionId(v.getOpcion() != null ? v.getOpcion().getId() : null)
                .valor(v.getValor())
                .orden(v.getOrden())
                .build();
    }

    public static List<OpcionValorResponseDTO> toResponseList(List<OpcionValor> list) {
        if (list == null) return List.of();
        return list.stream().map(OpcionValorMapper::toResponse).collect(Collectors.toList());
    }

    public static OpcionValor fromCrear(OpcionValorCrearDTO dto) {
        if (dto == null) return null;
        OpcionValor v = new OpcionValor();
        v.setValor(dto.getValor());
        v.setSlug(dto.getSlug());
        v.setOrden(dto.getOrden() != null ? dto.getOrden() : 0);
        // opcion debe asignarse en el servicio usando opcionId
        return v;
    }

    public static OpcionValor fromCrearWithOpcion(OpcionValorCrearDTO dto, Opcion opcion) {
        OpcionValor v = fromCrear(dto);
        if (v != null) v.setOpcion(opcion);
        return v;
    }

    public static void applyActualizar(OpcionValorActualizarDTO dto, OpcionValor entidad) {
        if (dto == null || entidad == null) return;
        if (dto.getValor() != null) entidad.setValor(dto.getValor());
        if (dto.getSlug() != null) entidad.setSlug(dto.getSlug());
        if (dto.getOrden() != null) entidad.setOrden(dto.getOrden());
    }
}