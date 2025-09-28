package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITokenResetPasswordRepository extends JpaRepository<TokenResetPassword, Long> {

    Optional<TokenResetPassword> findByUsuario_IdUsuarioAndUsadoFalse(Long usuarioId);
    
    Optional<TokenResetPassword> findByTokenAndUsadoFalse(String token);

    Optional<TokenResetPassword> findByToken(String token);
    
    Optional<TokenResetPassword> findTopByUsuarioOrderByIdDesc(Usuario usuario);

    Optional<TokenResetPassword> findByUsuario_IdUsuario(Long idUsuario);

    List<TokenResetPassword> findAllByUsuario_IdUsuario(Long usuarioId);
}