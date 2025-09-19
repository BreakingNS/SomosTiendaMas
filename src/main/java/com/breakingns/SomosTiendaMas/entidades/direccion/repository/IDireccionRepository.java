package com.breakingns.SomosTiendaMas.entidades.direccion.repository;

import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IDireccionRepository extends JpaRepository<Direccion, Long> {

    // buscar por usuario usando el nombre exacto de la propiedad en Usuario
    List<Direccion> findByUsuario_IdUsuario(Long idUsuario);

    List<Direccion> findByPerfilEmpresa_IdPerfilEmpresa(Long idPerfilEmpresa);

    // equivalente con filtro por tipo
    List<Direccion> findByUsuario_IdUsuarioAndTipo(Long idUsuario, String tipo);
    
    void deleteAllByPerfilEmpresa_IdPerfilEmpresa(Long idPerfilEmpresa);
    void deleteAllByUsuario_IdUsuario(Long idUsuario);

    // ...eliminar los métodos findByUsuarioId y findByUsuarioIdAndTipo existentes...
}
