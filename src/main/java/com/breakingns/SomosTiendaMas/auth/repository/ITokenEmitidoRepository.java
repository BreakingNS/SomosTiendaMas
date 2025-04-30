package com.breakingns.SomosTiendaMas.auth.repository;

import com.breakingns.SomosTiendaMas.auth.model.TokenEmitido;
import org.springframework.data.jpa.repository.Query;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ITokenEmitidoRepository extends JpaRepository<TokenEmitido, Long> {
    Optional<TokenEmitido> findByToken(String token);
    List<TokenEmitido> findAllByUsuario_Username(String username);

    @Modifying
    @Transactional
    @Query("UPDATE TokenEmitido t SET t.revocado = true WHERE t.token = :token")
    void revocarPorToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("UPDATE TokenEmitido t SET t.revocado = true WHERE t.usuario.username = :username AND t.revocado = false AND t.fechaExpiracion > :ahora")
    void revocarTokensActivosPorUsuario(@Param("username") String username, @Param("ahora") Instant ahora);

    List<TokenEmitido> findAllByUsuario_UsernameAndRevocadoFalse(String username);

    List<TokenEmitido> findAllByUsuario_IdUsuarioAndRevocadoFalse(Long idUsuario);
}