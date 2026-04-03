package com.breakingns.SomosTiendaMas.entidades.direccion.repository;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByPerfilUsuario_Id(Long perfilUsuarioId);

    List<Direccion> findByPerfilEmpresa_Id(Long perfilEmpresaId);

    void deleteByPerfilUsuario_Id(Long perfilUsuarioId);

    void deleteByPerfilEmpresa_Id(Long perfilEmpresaId);

    Optional<Direccion> findByPerfilUsuario_IdAndEsPrincipalTrue(Long perfilUsuarioId);

    Optional<Direccion> findByPerfilEmpresa_IdAndEsPrincipalTrue(Long perfilEmpresaId);

    List<Direccion> findByCopiedFromDireccion_Id(Long copiedFromId);

    // Historial / copias
    List<Direccion> findByCopiedFromDireccion_IdOrderByCreatedAtDesc(Long copiedFromId);

    Optional<Direccion> findFirstByCopiedFromDireccion_IdOrderByCreatedAtDesc(Long copiedFromId);

    Optional<Direccion> findByIdAndCopiedFromDireccion_Id(Long id, Long copiedFromId);

    // Filtrado por origen / sincronización
    List<Direccion> findByOrigen(Direccion.Origen origen);

    List<Direccion> findBySyncEnabledTrue();

    // Búsquedas por Usuario (a través de Perfil -> Usuario)
    List<Direccion> findByPerfilUsuario_Usuario_IdUsuario(Long usuarioId);

    List<Direccion> findByPerfilEmpresa_Usuario_IdUsuario(Long usuarioId);

    Optional<Direccion> findByPerfilUsuario_Usuario_IdUsuarioAndEsPrincipalTrue(Long usuarioId);

    Optional<Direccion> findByPerfilEmpresa_Usuario_IdUsuarioAndEsPrincipalTrue(Long usuarioId);

    // Variantes explícitas por el nombre de PK de PerfilEmpresa (idPerfilEmpresa)
    List<Direccion> findByPerfilEmpresa_IdPerfilEmpresa(Long perfilEmpresaId);

    void deleteByPerfilEmpresa_IdPerfilEmpresa(Long perfilEmpresaId);

    Optional<Direccion> findByPerfilEmpresa_IdPerfilEmpresaAndEsPrincipalTrue(Long perfilEmpresaId);

    @Query("SELECT d FROM Direccion d LEFT JOIN FETCH d.copiedFromDireccion WHERE d.id = :id")
    Optional<Direccion> findByIdWithCopiedFrom(@Param("id") Long id);
}
