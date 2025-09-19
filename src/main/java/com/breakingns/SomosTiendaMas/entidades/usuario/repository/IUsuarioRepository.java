package com.breakingns.SomosTiendaMas.entidades.usuario.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long>{
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByUsername(String username);
    
    Boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    List<Usuario> findByActivo(boolean b);

    List<Usuario> findByRolNombre(RolNombre rolNombre);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.direcciones WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithDirecciones(@Param("username") String username);
}
