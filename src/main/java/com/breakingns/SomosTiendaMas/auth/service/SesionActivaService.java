package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.AccesoDenegadoException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoEncontradaException;
import com.breakingns.SomosTiendaMas.security.exception.TokenNoEncontradoException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SesionActivaService {

    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IRefreshTokenRepository refreshTokenRepository;

    @Autowired
    public SesionActivaService(IUsuarioRepository usuarioRepository, 
                               ISesionActivaRepository sesionActivaRepository, 
                               ITokenEmitidoRepository tokenEmitidoRepository,
                               IRefreshTokenRepository refreshTokenRepository,
                               JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.sesionActivaRepository = sesionActivaRepository;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    // Listar sesiones activas de un usuario
    public List<SesionActivaDTO> listarSesionesActivas(Long usuarioId) {
        log.info("Por listar sesiones activas de user id: {}", usuarioId);
        return sesionActivaRepository.findByUsuario_IdUsuario(usuarioId).stream()
            .filter(sesion -> !sesion.isRevocado()) // Filtrar solo las activas
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    
    // Revocar sesión
    public void revocarSesion(String token) {
        log.info("Por revocar sesion, cuyo token es: {}", token);
        sesionActivaRepository.findByToken(token)
            .ifPresentOrElse(sesion -> {
                sesion.setRevocado(true);
                sesionActivaRepository.save(sesion);
            }, () -> { throw new TokenNoEncontradoException("Token no encontrado"); });
    }
    
    // Cerrar sesión del usuario
    public void cerrarSesion(Long idSesion) {
        log.info("Por cerrar sesion del user id: {}", idSesion);
        UserAuthDetails userDetails = (UserAuthDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SesionActiva sesion = sesionActivaRepository.findById(idSesion)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión no encontrada"));

        if (!sesion.getUsuario().getIdUsuario().equals(userDetails.getId())) {
            throw new AccesoDenegadoException("No tenés permiso para cerrar esta sesión");
        }
        sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);
        tokenEmitidoRepository.revocarPorToken(sesion.getToken());
    }

    // Revocar todas las sesiones de un usuario
    public void revocarTodasLasSesiones(String username) {
        log.info("Por revocar todas las sesiones del user: {}", username);
        List<SesionActiva> sesiones = sesionActivaRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        sesiones.forEach(sesion -> sesion.setRevocado(true));
        sesionActivaRepository.saveAll(sesiones);
    }

    // Revocar todas las sesiones excepto la actual
    public void revocarTodasLasSesionesExceptoSesionActual(Long idUsuario, String accessToken) {
        log.info("Por revocar todas las sesiones del user, excepto la actual: {}", idUsuario);
        List<SesionActiva> sesiones = sesionActivaRepository.findAllByUsuario_IdUsuarioAndRevocadoFalse(idUsuario);
        sesiones.forEach(sesion -> {
            if (!sesion.getToken().equals(accessToken)) {
                sesion.setRevocado(true);
            }
        });
        sesionActivaRepository.saveAll(sesiones);
    }
    
    // Registrar sesión activa
    public void registrarSesion(Usuario usuario, String token, String ip, String userAgent) {
        log.info("Registrando sesion de usuario: {}", usuario.getUsername());
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtTokenProvider.getJwtExpirationMs());
        SesionActiva sesion = new SesionActiva();
        sesion.setUsuario(usuario);
        sesion.setToken(token);
        sesion.setIp(ip);
        sesion.setUserAgent(userAgent);
        sesion.setFechaInicioSesion(now);
        sesion.setFechaExpiracion(expiry);
        sesion.setRevocado(false);
        sesionActivaRepository.save(sesion);
    }

    // Convertir sesión a DTO
    private SesionActivaDTO convertirADTO(SesionActiva sesion) {
        log.info("Convirtiendo a DTO:  {}", sesion.getUsuario().getUsername());
        return new SesionActivaDTO(
            sesion.getId(), 
            sesion.getIp(), 
            sesion.getUserAgent(),
            sesion.getFechaInicioSesion(), 
            sesion.getFechaExpiracion(),
            sesion.isRevocado()
        );
    }
}