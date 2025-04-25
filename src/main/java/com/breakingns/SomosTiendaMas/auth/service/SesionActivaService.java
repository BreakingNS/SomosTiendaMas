package com.breakingns.SomosTiendaMas.auth.service;

import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaDTO;
import com.breakingns.SomosTiendaMas.auth.dto.SesionActivaResponse;
import com.breakingns.SomosTiendaMas.auth.model.RefreshToken;
import com.breakingns.SomosTiendaMas.auth.model.SesionActiva;
import com.breakingns.SomosTiendaMas.auth.repository.IRefreshTokenRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ISesionActivaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ITokenEmitidoRepository;
import com.breakingns.SomosTiendaMas.domain.usuario.model.Usuario;
import com.breakingns.SomosTiendaMas.domain.usuario.repository.IUsuarioRepository;
import static jakarta.persistence.GenerationType.UUID;
import jakarta.servlet.http.HttpServletRequest;
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

    private final IUsuarioRepository usuarioRepository;
    private final ISesionActivaRepository sesionActivaRepository;
    private final ITokenEmitidoRepository tokenEmitidoRepository;
    private final IRefreshTokenRepository refreshTokenRepository;

    @Autowired
    public SesionActivaService(IUsuarioRepository usuarioRepository, 
                                ISesionActivaRepository sesionActivaRepository, 
                                ITokenEmitidoRepository tokenEmitidoRepository,
                                IRefreshTokenRepository refreshTokenRepository
                                ) {
        this.usuarioRepository = usuarioRepository;
        this.sesionActivaRepository = sesionActivaRepository;
        this.tokenEmitidoRepository = tokenEmitidoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }
    
    public List<SesionActivaDTO> listarSesionesPorUsuario(Long usuarioId) {
        return sesionActivaRepository.findByUsuario_IdUsuario(usuarioId).stream()
            .map(s -> new SesionActivaDTO(
                s.getId(), s.getIp(), s.getUserAgent(),
                s.getFechaInicioSesion(), s.getFechaExpiracion(), s.isRevocado()
            ))
            .toList();
    }

    public void revocarSesion(String token) {
        SesionActiva sesion = sesionActivaRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token no encontrado"));
        sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);
    }

    public List<SesionActivaResponse> obtenerSesionesActivas(Usuario usuario) {
        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario(usuario);

        return sesiones.stream()
            .map(s -> new SesionActivaResponse(
                s.getId(),
                s.getIp(),
                s.getUserAgent(),
                s.getFechaInicioSesion(),
                s.isRevocado()
            ))
            .collect(Collectors.toList());
    }
    public void cerrarSesion(Long idSesion) {
        // Obtener el usuario logueado desde el contexto
        Usuario usuarioActual = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Buscar la sesión
        SesionActiva sesion = sesionActivaRepository.findById(idSesion)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión no encontrada"));

        // Verificar que la sesión pertenezca al usuario logueado
        if (!sesion.getUsuario().getIdUsuario().equals(usuarioActual.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tenés permiso para cerrar esta sesión");
        }

        // Marcar como inactiva / revocada
        sesion.setRevocado(true); // o sesion.setRevocado(true);
        sesionActivaRepository.save(sesion);

        // (Opcional) agregar a blacklist
        tokenEmitidoRepository.revocarPorToken(sesion.getToken());
    }
    
    public void cerrarOtrasSesiones(String tokenActual) {
        Usuario usuarioActual = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<SesionActiva> sesiones = sesionActivaRepository.findByUsuario_IdUsuarioAndRevocadoFalse(usuarioActual.getIdUsuario());

        for (SesionActiva sesion : sesiones) {
            if (!sesion.getToken().equals(tokenActual)) {
                sesion.setRevocado(true);
                sesionActivaRepository.save(sesion);

                tokenEmitidoRepository.findByToken(sesion.getToken())
                    .ifPresent(tokenEmitido -> {
                        tokenEmitido.setRevocado(true);
                        tokenEmitidoRepository.save(tokenEmitido);
                    });
            }
        }
    }
    
    public void revocarTodasLasSesiones(String username) {
        List<SesionActiva> sesiones = sesionActivaRepository.findAllByUsuario_UsernameAndRevocadoFalse(username);
        sesiones.forEach(sesion -> sesion.setRevocado(true));
        sesionActivaRepository.saveAll(sesiones);
    }
    
    public void registrarSesion(String jwt, Usuario usuario, String ip, String userAgent) {
        SesionActiva sesion = new SesionActiva();
        sesion.setToken(jwt);
        sesion.setUsuario(usuario);
        sesion.setIp(ip);
        sesion.setUserAgent(userAgent);
        sesion.setFechaInicioSesion(Instant.now());
        sesion.setFechaExpiracion(Instant.now().plusSeconds(3600)); // o lo que dure tu token
        sesion.setRevocado(false);
        sesionActivaRepository.save(sesion);
    }
}