package com.breakingns.SomosTiendaMas.auth.service.util;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoEncontradaException;

@Service
public class RevocacionUtils {
    private final ISesionActivaRepository sesionActivaRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IRefreshTokenRepository refreshTokenRepository;

    public RevocacionUtils(
        ISesionActivaRepository sesionActivaRepository,
        ITokenEmitidoRepository tokenEmitidoRepository,
        IRefreshTokenRepository refreshTokenRepository
    ) {
        this.sesionActivaRepository = sesionActivaRepository;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void revocarTodoPorSesion(Long idSesion) {
        SesionActiva sesion = sesionActivaRepository.findById(idSesion)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión no encontrada"));

        // Marcar la sesión como revocada
        sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);

        // Revocar access token
        tokenEmitidoRepository.revocarPorToken(sesion.getToken());

        // Revocar refresh token
        refreshTokenRepository.revocarPorToken(sesion.getRefreshToken(), Instant.now());
    }
}
