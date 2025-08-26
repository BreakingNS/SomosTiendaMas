package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.shared.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
//import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.auth.service.util.RevocacionUtils;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.AccesoDenegadoException;
import com.breakingns.SomosTiendaMas.security.exception.SesionActivaNoEncontradaException;
import com.breakingns.SomosTiendaMas.security.exception.SesionNoEncontradaException;
import com.breakingns.SomosTiendaMas.security.exception.UsuarioNoEncontradoException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SesionActivaService {

    private final JwtTokenProvider jwtTokenProvider;
    
    private final TokenEmitidoService tokenEmitidoService;

    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    //private final ITokenEmitidoRepository tokenEmitidoRepository;

    private final RevocacionUtils revocacionUtils;

    @Autowired
    public SesionActivaService(JwtTokenProvider jwtTokenProvider, 
                                TokenEmitidoService tokenEmitidoService, 
                                IUsuarioRepository usuarioRepository, 
                                ISesionActivaRepository sesionActivaRepository, 
                                //ITokenEmitidoRepository tokenEmitidoRepository,
                                RevocacionUtils revocacionUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenEmitidoService = tokenEmitidoService;
        this.usuarioRepository = usuarioRepository;
        this.sesionActivaRepository = sesionActivaRepository;
        //this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.revocacionUtils = revocacionUtils;
    }
    
    // Método actual reforzado: solo para uso normal de usuario autenticado
    public List<SesionActivaDTO> listarSesionesActivas(Long usuarioId) {
        Long idAutenticado = tokenEmitidoService.obtenerIdDesdeToken();

        if (!usuarioId.equals(idAutenticado)) {
            throw new AccesoDenegadoException("No puedes ver sesiones de otro usuario.");
        }

        return sesionActivaRepository.findByUsuario_IdUsuario(usuarioId).stream()
            .filter(s -> !s.isRevocado())
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    // Nuevo método exclusivo para ADMIN
    public List<SesionActivaDTO> listarSesionesActivasComoAdmin(Long idUsuario) {
        // Si idUsuario es null, devolvemos todas las sesiones activas
        if (idUsuario == null) {
            return sesionActivaRepository.findAll().stream()
                .filter(sesion -> !sesion.isRevocado())
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        }

        // Validamos que el usuario exista
        usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario con ID " + idUsuario + " no existe"));

        // Si el usuario existe, buscamos sus sesiones activas
        List<SesionActiva> sesionesActivas = sesionActivaRepository.findByUsuario_IdUsuario(idUsuario)
            .stream()
            .filter(sesion -> !sesion.isRevocado())
            .collect(Collectors.toList());

        if (sesionesActivas.isEmpty()) {
            throw new SesionActivaNoEncontradaException("No hay sesiones activas para el usuario con ID " + idUsuario);
        }

        return sesionesActivas.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
    /* 
    public void revocarSesionCompleta(String accessToken) {
        sesionActivaRepository.findByToken(accessToken)
            .ifPresentOrElse(sesion -> {
                sesion.setRevocado(true);
                sesionActivaRepository.save(sesion);

                // Revocar access token
                tokenEmitidoRepository.revocarPorToken(sesion.getToken());

                // Revocar refresh token
                if (sesion.getRefreshToken() != null && !sesion.getRefreshToken().isEmpty()) {
                    refreshTokenService.revocarPorToken(sesion.getRefreshToken());
                }
            }, () -> { throw new TokenNoEncontradoException("Token no encontrado"); });
    }*/

    /*
    // Revocar sesión
    public void revocarSesion(String token, String refreshToken) {
        log.info("Por revocar sesion, cuyo token es: {}", token);
        sesionActivaRepository.findByToken(token)
            .ifPresentOrElse(sesion -> {
                sesion.setRevocado(true);
                sesionActivaRepository.save(sesion);
            }, () -> { throw new TokenNoEncontradoException("Token no encontrado"); });
    }*/
    
    // Cerrar sesión del usuario
    public void cerrarSesion(Long idSesion) {
        log.info("Por cerrar sesion del user id: {}", idSesion);
        UserAuthDetails userDetails = (UserAuthDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SesionActiva sesion = sesionActivaRepository.findById(idSesion)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión no encontrada"));

        if (!sesion.getUsuario().getIdUsuario().equals(userDetails.getId())) {
            throw new AccesoDenegadoException("No tenés permiso para cerrar esta sesión");
        }
        revocarSesion(sesion); // Reutiliza la lógica centralizada
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
    public void registrarSesion(Usuario usuario, String token, String refresh, String ip, String userAgent) {
        log.info("Registrando sesion de usuario: {}", usuario.getUsername());
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtTokenProvider.getJwtExpirationMs());
        SesionActiva sesion = new SesionActiva();
        sesion.setUsuario(usuario);
        sesion.setToken(token);
        sesion.setRefreshToken(refresh);
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
    
    public Integer numeroSesionesActivas(Long idUsuario){
        List<SesionActiva> sesionesActivas = sesionActivaRepository.findAllByUsuario_IdUsuarioAndRevocadoFalse(idUsuario);
        
        return sesionesActivas.size();
    }

    public Optional<SesionActiva> buscarPorToken(String tokenAnterior) {
        return sesionActivaRepository.findByToken(tokenAnterior);
    }

    public void revocarSesionPorId(Long idSesion) {
        SesionActiva sesion = sesionActivaRepository.findById(idSesion)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión no encontrada"));
        revocarSesion(sesion);
    }

    public void revocarSesion(SesionActiva sesion) {
        revocacionUtils.revocarTodoPorSesion(sesion.getId());
    }

    public Optional<SesionActiva> buscarSesionPorTokenYUsuario(String accessToken, Long idUsuario) {
        return sesionActivaRepository.findByToken(accessToken)
            .filter(sesion -> sesion.getUsuario().getIdUsuario().equals(idUsuario));
    }

    public Long buscarPorRefreshToken(String token) {
        return sesionActivaRepository.findByRefreshToken(token)
            .map(SesionActiva::getId)
            .orElseThrow(() -> new SesionNoEncontradaException("Sesión no encontrada para el refresh token: " + token));
    }
}