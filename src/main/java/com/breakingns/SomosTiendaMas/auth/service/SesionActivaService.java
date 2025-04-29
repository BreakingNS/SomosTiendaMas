package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaResponse;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.model.UserAuthDetails;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.auth.security.jwt.JwtTokenProvider;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SesionActivaService {

    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final TokenEmitidoService tokenEmitidoService;

    @Autowired
    public SesionActivaService(IUsuarioRepository usuarioRepository, 
                                ISesionActivaRepository sesionActivaRepository, 
                                ITokenEmitidoRepository tokenEmitidoRepository,
                                IRefreshTokenRepository refreshTokenRepository,
                                TokenEmitidoService tokenEmitidoService,
                                JwtTokenProvider jwtTokenProvider
                                ) {
        this.usuarioRepository = usuarioRepository;
        this.sesionActivaRepository = sesionActivaRepository;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenEmitidoService = tokenEmitidoService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    public List<SesionActivaDTO> listarSesionesPorUsuario(Long usuarioId) {
        return sesionActivaRepository.findByUsuario_IdUsuario(usuarioId).stream()
            .map(s -> new SesionActivaDTO(
                s.getId(), 
                s.getIp(), 
                s.getUserAgent(),
                s.getFechaInicioSesion(), 
                s.getFechaExpiracion(),
                s.isRevocado()
            ))
            .toList();
    }

    public void revocarSesion(String token) {
        SesionActiva sesion = sesionActivaRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token no encontrado"));
        sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);
    }

    public List<SesionActivaDTO> obtenerSesionesActivas(Long idUsuario) {
        if (idUsuario != null) {
            return obtenerSesionesActivasPorUsuario(idUsuario);
        } else {
            return obtenerTodasLasSesionesActivas();
        }
    }

    public List<SesionActivaDTO> obtenerSesionesActivasPorUsuario(Long idUsuario) {
        return sesionActivaRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .filter(sesion -> !sesion.isRevocado())
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<SesionActivaDTO> obtenerTodasLasSesionesActivas() {
        return sesionActivaRepository.findAll()
                .stream()
                .filter(sesion -> !sesion.isRevocado())
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    public void cerrarSesion(Long idSesion) {
        // Obtener el usuario logueado desde el contexto
        UserAuthDetails userDetails = (UserAuthDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long idUsuario = userDetails.getId();
        
        // Buscar la sesión
        SesionActiva sesion = sesionActivaRepository.findById(idSesion)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión no encontrada"));

        // Verificar que la sesión pertenezca al usuario logueado
        if (!sesion.getUsuario().getIdUsuario().equals(userDetails.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso para cerrar esta sesión");
        }

        // Marcar como inactiva / revocada
        sesion.setRevocado(true); // o sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);

        // (Opcional) agregar a blacklist
        tokenEmitidoRepository.revocarPorToken(sesion.getToken());
    }
    
    public void revocarTodasLasSesiones(String username) {
        List<SesionActiva> sesiones = sesionActivaRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        sesiones.forEach(sesion -> sesion.setRevocado(true));
        sesionActivaRepository.saveAll(sesiones);
    }
    
    public void revocarTodasLasSesionesExceptoSesionActual(Long idUsuario, String accessToken) {
        List<SesionActiva> sesiones = sesionActivaRepository.findAllByUsuario_IdUsuarioAndRevocadoFalse(idUsuario);
        sesiones.forEach(sesion -> {
            if (!sesion.getToken().equals(accessToken)) { // Revocar todas excepto la sesión actual
                sesion.setRevocado(true);
            }
        });
        sesionActivaRepository.saveAll(sesiones);
    }
    
    public void registrarSesion(Usuario usuario, String token, String ip, String userAgent) {
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

    private SesionActivaDTO convertirADTO(SesionActiva sesion) {
        // Podés adaptar el DTO según tus campos
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