package com.breakingns.SomosTiendaMas.entidades.usuario.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long>{
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByUsername(String username);
    
    Boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
}
