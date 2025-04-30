package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    int deleteByUsuario(Usuario usuario);

    List<RefreshToken> findAllByUsuarioAndRevocadoFalse(Usuario usuario);
    
    List<RefreshToken> findByUsuarioUsername(String username);

    List<RefreshToken> findAllByUsuario_UsernameAndRevocadoFalse(String username);

    List<RefreshToken> findAllByUsuario_IdUsuarioAndRevocadoFalse(Long idUsuario);
}