package com.breakingns.SomosTiendaMas.repository;

import com.breakingns.SomosTiendaMas.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long>{
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    Boolean existsByUsername(String username);
}
