package com.breakingns.SomosTiendaMas.entidades.perfil.repository;

import com.breakingns.SomosTiendaMas.entidades.perfil.model.Perfil;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByUsuario(Usuario usuario);
    Optional<Perfil> findByUsuario_IdUsuario(Long usuarioId);
    Optional<Perfil> findByDocumento(String documento);

    List<Perfil> findByActivo(boolean activo);

    @Query("SELECT DISTINCT p FROM Perfil p " +
           "LEFT JOIN FETCH p.direcciones d " +
           "LEFT JOIN FETCH p.telefonos t " +
           "WHERE p.id = :id")
    Optional<Perfil> findByIdWithDireccionesYTelefonos(@Param("id") Long id);
}
