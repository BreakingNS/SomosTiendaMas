package com.breakingns.SomosTiendaMas.entidades.telefono.mapper;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
    public interface TelefonoMapper {
        Telefono toEntity(TelefonoCreateDTO dto);

        @Mapping(target = "ownerType", source = ".", qualifiedByName = "resolveOwnerType")
        @Mapping(target = "ownerId", source = ".", qualifiedByName = "resolveOwnerId")
        TelefonoResponseDTO toDto(Telefono telefono);

        @Named("resolveOwnerType")
        default String resolveOwnerType(Telefono telefono) {
            if (telefono == null) return null;
            if (telefono.getPerfilUsuario() != null) return "USUARIO";
            if (telefono.getPerfilEmpresa() != null) return "EMPRESA";
            return null;
        }

        @Named("resolveOwnerId")
        default Long resolveOwnerId(Telefono telefono) {
            if (telefono == null) return null;
            if (telefono.getPerfilUsuario() != null) {
                try {
                    return telefono.getPerfilUsuario().getId();
                } catch (Exception e) {
                    return null;
                }
            }
            if (telefono.getPerfilEmpresa() != null) {
                try {
                    return telefono.getPerfilEmpresa().getIdPerfilEmpresa();
                } catch (Exception e) {
                    try {
                        return telefono.getPerfilEmpresa().getId();
                    } catch (Exception ex) {
                        return null;
                    }
                }
            }
            return null;
        }
}
