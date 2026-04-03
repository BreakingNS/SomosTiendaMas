package com.breakingns.SomosTiendaMas.entidades.usuario.mapper;

import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.dto.UsuarioUpdateDTO;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponseDTO toDto(Usuario usuario);

    Usuario toEntity(UsuarioCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UsuarioUpdateDTO dto, @MappingTarget Usuario entity);
}
