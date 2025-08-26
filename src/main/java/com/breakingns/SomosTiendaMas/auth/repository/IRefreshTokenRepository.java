package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    int deleteByUsuario(Usuario usuario);

    List<RefreshToken> findAllByUsuarioAndRevocadoFalse(Usuario usuario);
    
    List<RefreshToken> findByUsuarioUsername(String username);

    List<RefreshToken> findAllByUsuario_UsernameAndRevocadoFalse(String username);

    List<RefreshToken> findAllByUsuario_IdUsuarioAndRevocadoFalse(Long idUsuario);

    boolean existsByToken(String refreshToken);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revocado = true, r.fechaRevocado = :fechaRevocado WHERE r.token = :token")
    void revocarPorToken(@Param("token") String token, @Param("fechaRevocado") Instant fechaRevocado);
    
    List<RefreshToken> findAllByUsuario_Username(String username);

    /*
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.token = :token")
    void revocarPorToken(@Param("token") String token);*/
}