package com.breakingns.SomosTiendaMas.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.breakingns.SomosTiendaMas.auth.model.EventoAuditoria;

public interface IEventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {
    boolean existsByUsernameAndTipoEvento(String username, String tipoEvento);
}
