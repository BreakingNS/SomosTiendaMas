package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SesionActivaService {

    private final ISesionActivaRepository sesionRepository;
    private final IUsuarioRepository usuarioRepository;

    public List<SesionActivaDTO> listarSesionesPorUsuario(Long usuarioId) {
        return sesionRepository.findByUsuario_IdUsuario(usuarioId).stream()
            .map(s -> new SesionActivaDTO(
                s.getId(), s.getIp(), s.getUserAgent(),
                s.getFechaInicioSesion(), s.getFechaExpiracion(), s.isRevocado()
            ))
            .toList();
    }

    public void revocarSesion(String token) {
        SesionActiva sesion = sesionRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token no encontrado"));
        sesion.setRevocado(true);
        sesionRepository.save(sesion);
    }
}