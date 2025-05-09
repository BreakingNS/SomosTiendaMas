package com.breakingns.SomosTiendaMas.domain.usuario.repository;

import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long>{
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByUsername(String username);
    
    Boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
}
