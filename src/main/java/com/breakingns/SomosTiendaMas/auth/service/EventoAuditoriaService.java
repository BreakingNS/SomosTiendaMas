package com.breakingns.SomosTiendaMas.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.breakingns.SomosTiendaMas.auth.model.EventoAuditoria;
import com.breakingns.SomosTiendaMas.auth.repository.IEventoAuditoriaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class EventoAuditoriaService {
    private final IEventoAuditoriaRepository eventoAuditoriaRepository;

    public EventoAuditoriaService(IEventoAuditoriaRepository eventoAuditoriaRepository) {
        this.eventoAuditoriaRepository = eventoAuditoriaRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(String username, String tipoEvento, String descripcion) {
        System.out.println("[AUDITORIA] Registrando evento: usuario=" + username + ", tipo=" + tipoEvento + ", descripcion=" + descripcion);

        EventoAuditoria evento = new EventoAuditoria();
        evento.setUsername(username);
        evento.setTipoEvento(tipoEvento);
        evento.setDescripcion(descripcion);
        evento.setFecha(LocalDateTime.now());
        eventoAuditoriaRepository.save(evento);
    }
}
