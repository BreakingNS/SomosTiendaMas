package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.TokenResetPassword;
import com.breakingns.SomosTiendaMas.entidades.usuario.model.Usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITokenResetPasswordRepository extends JpaRepository<TokenResetPassword, Long> {

    Optional<TokenResetPassword> findByToken(String token);
    
    Optional<TokenResetPassword> findTopByUsuarioOrderByIdDesc(Usuario usuario);
}