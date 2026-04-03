package com.breakingns.SomosTiendaMas.entidades.telefono.repository;

import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio unificado para `Telefono`.
 * Reemplaza a las implementaciones específicas `TelefonoUsuarioRepository` y `TelefonoEmpresaRepository`.
 */
public interface TelefonoRepository extends JpaRepository<Telefono, Long> {

    // Finders compatibles con los repos antiguos
    List<Telefono> findByPerfilUsuarioId(Long perfilUsuarioId);
    List<Telefono> findByPerfilEmpresaIdPerfilEmpresa(Long perfilEmpresaId);

    // Eliminaciones masivas por owner
    void deleteByPerfilUsuarioId(Long perfilUsuarioId);
    void deleteByPerfilEmpresaIdPerfilEmpresa(Long perfilEmpresaId);

    // Favoritos / primarios
    List<Telefono> findByPerfilUsuarioIdAndFavoritoTrue(Long perfilUsuarioId);
    List<Telefono> findByPerfilEmpresaIdPerfilEmpresaAndFavoritoTrue(Long perfilEmpresaId);

    // Soporte para detectar copias
    Optional<Telefono> findByIdAndCopiadaDeTelefonoId(Long id, Long copiadaDeId);

    // Historial / copias
    List<Telefono> findByCopiadaDeTelefono_Id(Long copiadaDeId);

    List<Telefono> findByCopiadaDeTelefono_IdOrderByCreatedAtDesc(Long copiadaDeId);

    Optional<Telefono> findFirstByCopiadaDeTelefono_IdOrderByCreatedAtDesc(Long copiadaDeId);

    Optional<Telefono> findByIdAndCopiadaDeTelefono_Id(Long id, Long copiadaDeId);

    // Filtrado por origen / sincronización
    List<Telefono> findByOrigen(Telefono.Origen origen);

    List<Telefono> findBySyncEnabledTrue();

    // Búsquedas por Usuario (a través de Perfil -> Usuario)
    List<Telefono> findByPerfilUsuario_Usuario_IdUsuario(Long usuarioId);
    List<Telefono> findByPerfilEmpresa_Usuario_IdUsuario(Long usuarioId);

    // Búsquedas de teléfono "principal" por tipo o por favorito (según modelo)
    Optional<Telefono> findFirstByPerfilUsuario_Usuario_IdUsuarioAndTipoTelefono(Long usuarioId, Telefono.TipoTelefono tipoTelefono);
    Optional<Telefono> findFirstByPerfilEmpresa_Usuario_IdUsuarioAndTipoTelefono(Long usuarioId, Telefono.TipoTelefono tipoTelefono);

    Optional<Telefono> findFirstByPerfilUsuario_Usuario_IdUsuarioAndFavoritoTrue(Long usuarioId);
    Optional<Telefono> findFirstByPerfilEmpresa_Usuario_IdUsuarioAndFavoritoTrue(Long usuarioId);

    // Variantes explícitas por el nombre de PK de PerfilEmpresa (idPerfilEmpresa)
    List<Telefono> findByPerfilEmpresa_IdPerfilEmpresa(Long perfilEmpresaId);

    void deleteByPerfilEmpresa_IdPerfilEmpresa(Long perfilEmpresaId);

    List<Telefono> findByPerfilEmpresa_IdPerfilEmpresaAndFavoritoTrue(Long perfilEmpresaId);

    Optional<Telefono> findFirstByPerfilEmpresa_IdPerfilEmpresaAndTipoTelefono(Long perfilEmpresaId, Telefono.TipoTelefono tipoTelefono);

    Optional<Telefono> findFirstByPerfilEmpresa_IdPerfilEmpresaAndFavoritoTrue(Long perfilEmpresaId);
}
