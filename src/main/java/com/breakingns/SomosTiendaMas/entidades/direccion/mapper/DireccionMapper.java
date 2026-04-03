package com.breakingns.SomosTiendaMas.entidades.direccion.mapper;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DireccionMapper {

	Direccion toEntity(DireccionCreateDTO dto);

	@Mapping(target = "ownerType", source = ".", qualifiedByName = "resolveOwnerType")
	@Mapping(target = "ownerId", source = ".", qualifiedByName = "resolveOwnerId")
	@Mapping(target = "copiadaDeDireccionId", expression = "java(direccion.getCopiedFromDireccion() != null ? direccion.getCopiedFromDireccion().getId() : null)")
	@Mapping(target = "canonicalAddressId", source = "canonicalAddressId")
	@Mapping(target = "origen", expression = "java(direccion.getOrigen() != null ? direccion.getOrigen().name() : null)")
	@Mapping(target = "originOwnerType", expression = "java(direccion.getOriginOwnerType() != null ? direccion.getOriginOwnerType().name() : null)")
	@Mapping(target = "originOwnerId", source = "originOwnerId")
	@Mapping(target = "syncEnabled", source = "syncEnabled")
	@Mapping(target = "notas", source = "notas")
	@Mapping(target = "version", source = "version")
	DireccionResponseDTO toDto(Direccion direccion);

	// métodos auxiliares usados por MapStruct (manejan nulos y nombres de PK distintos)
	@Named("resolveOwnerType")
	default String resolveOwnerType(Direccion direccion) {
		if (direccion == null) return null;
		if (direccion.getPerfilUsuario() != null) return "USUARIO";
		if (direccion.getPerfilEmpresa() != null) return "EMPRESA";
		return null;
	}

	@Named("resolveOwnerId")
	default Long resolveOwnerId(Direccion direccion) {
		if (direccion == null) return null;
		if (direccion.getPerfilUsuario() != null) {
			try {
				return direccion.getPerfilUsuario().getId();
			} catch (Exception e) {
				return null;
			}
		}
		if (direccion.getPerfilEmpresa() != null) {
			try {
				return direccion.getPerfilEmpresa().getIdPerfilEmpresa();
			} catch (Exception e) {
				try {
					return direccion.getPerfilEmpresa().getId();
				} catch (Exception ex) {
					return null;
				}
			}
		}
		return null;
	}
}
