package com.breakingns.SomosTiendaMas.entidades.perfil_empresa.repository;

import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilEmpresaRepository extends JpaRepository<PerfilEmpresa, Long> {

    Optional<PerfilEmpresa> findByCuit(String cuit);

    boolean existsByCuit(String cuit);

    Optional<PerfilEmpresa> findByRazonSocial(String razonSocial);

    Optional<PerfilEmpresa> findByUsuario(Usuario usuario);

    Optional<PerfilEmpresa> findByUsuario_IdUsuario(Long usuarioId);

    List<PerfilEmpresa> findByEstadoAprobado(PerfilEmpresa.EstadoAprobado estadoAprobado);

    List<PerfilEmpresa> findByActivo(boolean activo);

    @Query("SELECT DISTINCT p FROM PerfilEmpresa p " +
           "LEFT JOIN FETCH p.direcciones d " +
           "LEFT JOIN FETCH p.telefonos t " +
           "WHERE p.idPerfilEmpresa = :id")
    Optional<PerfilEmpresa> findByIdWithDireccionesYTelefonos(@Param("id") Long id);

    // Búsquedas por empresaId (clave primaria usada en la entidad: idPerfilEmpresa)
    Optional<PerfilEmpresa> findByIdPerfilEmpresa(Long idPerfilEmpresa);

    boolean existsByIdPerfilEmpresa(Long idPerfilEmpresa);
}
