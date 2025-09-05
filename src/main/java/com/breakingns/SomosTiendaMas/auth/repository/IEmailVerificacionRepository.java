package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.EmailVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEmailVerificacionRepository extends JpaRepository<EmailVerificacion, Long> {
    Optional<EmailVerificacion> findByCodigoAndUsadoFalse(String codigo);
    Optional<EmailVerificacion> findByUsuario_IdUsuarioAndUsadoFalse(Long usuarioId);
    boolean existsByCodigoAndUsadoFalse(String codigo);
    Optional<EmailVerificacion> findByUsuario_IdUsuario(Long usuarioId);
}
