package com.breakingns.SomosTiendaMas.entidades.perfil.mapper;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.mapper.DireccionMapper;
import com.breakingns.SomosTiendaMas.entidades.perfil.dto.PerfilCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil.dto.PerfilUpdateDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.telefono.mapper.TelefonoMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = { DireccionMapper.class, TelefonoMapper.class })
public interface PerfilMapper {

    Perfil toEntity(PerfilCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PerfilUpdateDTO dto, @MappingTarget Perfil entity);
}
