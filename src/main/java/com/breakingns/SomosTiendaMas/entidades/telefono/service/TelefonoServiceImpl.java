package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.*;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.*;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.*;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model.PerfilUsuario;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TelefonoServiceImpl implements ITelefonoService {

    private final TelefonoUsuarioRepository telUsuarioRepo;
    private final TelefonoEmpresaRepository telEmpresaRepo;
    private final ICodigoAreaRepository codigoAreaRepo;

    public TelefonoServiceImpl(TelefonoUsuarioRepository telUsuarioRepo,
                               TelefonoEmpresaRepository telEmpresaRepo,
                               ICodigoAreaRepository codigoAreaRepo) {
        this.telUsuarioRepo = telUsuarioRepo;
        this.telEmpresaRepo = telEmpresaRepo;
        this.codigoAreaRepo = codigoAreaRepo;
    }

    @Override
    public TelefonoResponseDTO registrarTelefono(RegistroTelefonoDTO dto) {
        String caracteristica = sanitizeDigits(dto.getCaracteristica());
        String numero = sanitizeDigits(dto.getNumero());
        ensureCaracteristicaValida(caracteristica);
        ensureNumeroValido(numero);

        Boolean activo = dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE;
        Boolean verificado = dto.getVerificado() != null ? dto.getVerificado() : Boolean.FALSE;

        if (dto.getPerfilUsuarioId() != null) {
            TelefonoUsuario e = new TelefonoUsuario();
            PerfilUsuario p = new PerfilUsuario();
            p.setId(dto.getPerfilUsuarioId());
            e.setPerfilUsuario(p);

            e.setTipo(parseTipoUsuario(dto.getTipo()));
            e.setNumero(numero);
            e.setCaracteristica(caracteristica);
            e.setActivo(activo);
            e.setVerificado(verificado);
            e.setFavorito(dto.getFavorito() != null ? dto.getFavorito() : Boolean.FALSE);

            TelefonoUsuario saved = telUsuarioRepo.save(e);
            return toResponseDTO(saved);
        } else if (dto.getPerfilEmpresaId() != null) {
            TelefonoEmpresa e = new TelefonoEmpresa();
            PerfilEmpresa p = new PerfilEmpresa();
            // Nota: PerfilEmpresa suele exponer idPerfilEmpresa
            try {
                PerfilEmpresa.class.getMethod("setIdPerfilEmpresa", Long.class).invoke(p, dto.getPerfilEmpresaId());
            } catch (Exception ignore) {
                // fallback si el setter es setId
                try { PerfilEmpresa.class.getMethod("setId", Long.class).invoke(p, dto.getPerfilEmpresaId()); } catch (Exception ex) { /* no-op */ }
            }
            e.setPerfilEmpresa(p);

            e.setTipo(parseTipoEmpresa(dto.getTipo()));
            e.setNumero(numero);
            e.setCaracteristica(caracteristica);
            e.setActivo(activo);
            e.setVerificado(verificado);

            TelefonoEmpresa saved = telEmpresaRepo.save(e);
            return toResponseDTO(saved);
        }
        throw new IllegalArgumentException("Se requiere perfilUsuarioId o perfilEmpresaId");
    }

    @Override
    public TelefonoResponseDTO actualizarTelefono(Long id, ActualizarTelefonoDTO dto) {
        Optional<TelefonoUsuario> uOpt = telUsuarioRepo.findById(id);
        if (uOpt.isPresent()) {
            TelefonoUsuario e = uOpt.get();
            if (dto.getTipo() != null) e.setTipo(parseTipoUsuario(dto.getTipo()));
            if (dto.getCaracteristica() != null) {
                String car = sanitizeDigits(dto.getCaracteristica());
                ensureCaracteristicaValida(car);
                e.setCaracteristica(car);
            }
            if (dto.getNumero() != null) {
                String num = sanitizeDigits(dto.getNumero());
                ensureNumeroValido(num);
                e.setNumero(num);
            }
            if (dto.getActivo() != null) e.setActivo(dto.getActivo());
            if (dto.getVerificado() != null) e.setVerificado(dto.getVerificado());
            if (dto.getFavorito() != null) e.setFavorito(dto.getFavorito());

            return toResponseDTO(telUsuarioRepo.save(e));
        }

        Optional<TelefonoEmpresa> empOpt = telEmpresaRepo.findById(id);
        if (empOpt.isPresent()) {
            TelefonoEmpresa e = empOpt.get();
            if (dto.getTipo() != null) e.setTipo(parseTipoEmpresa(dto.getTipo()));
            if (dto.getCaracteristica() != null) {
                String car = sanitizeDigits(dto.getCaracteristica());
                ensureCaracteristicaValida(car);
                e.setCaracteristica(car);
            }
            if (dto.getNumero() != null) {
                String num = sanitizeDigits(dto.getNumero());
                ensureNumeroValido(num);
                e.setNumero(num);
            }
            if (dto.getActivo() != null) e.setActivo(dto.getActivo());
            if (dto.getVerificado() != null) e.setVerificado(dto.getVerificado());

            return toResponseDTO(telEmpresaRepo.save(e));
        }

        throw new IllegalArgumentException("Teléfono no encontrado con id: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    public TelefonoResponseDTO obtenerTelefono(Long id) {
        Optional<TelefonoUsuario> uOpt = telUsuarioRepo.findById(id);
        if (uOpt.isPresent()) return toResponseDTO(uOpt.get());
        Optional<TelefonoEmpresa> eOpt = telEmpresaRepo.findById(id);
        if (eOpt.isPresent()) return toResponseDTO(eOpt.get());
        throw new IllegalArgumentException("Teléfono no encontrado con id: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelefonoResponseDTO> listarTelefonosPorUsuario(Long perfilUsuarioId) {
        return telUsuarioRepo.findByPerfilUsuarioId(perfilUsuarioId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelefonoResponseDTO> listarTelefonosPorPerfilEmpresa(Long perfilEmpresaId) {
        return telEmpresaRepo.findByPerfilEmpresaIdPerfilEmpresa(perfilEmpresaId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // Nuevo: eliminar teléfonos por perfil de usuario
    public void eliminarTelefonosPorUsuario(Long perfilUsuarioId) {
        telUsuarioRepo.deleteByPerfilUsuarioId(perfilUsuarioId);
    }

    // Nuevo: eliminar teléfonos por perfil de empresa
    public void eliminarTelefonosPorPerfilEmpresa(Long perfilEmpresaId) {
        telEmpresaRepo.deleteByPerfilEmpresaIdPerfilEmpresa(perfilEmpresaId);
    }

    // Helpers

    private void ensureCaracteristicaValida(String codigo) {
        if (codigo == null || !codigo.matches("\\d+")) {
            throw new IllegalArgumentException("Característica inválida (debe contener solo dígitos).");
        }
        if (codigoAreaRepo.findByCodigo(codigo).isEmpty()) {
            throw new IllegalArgumentException("Característica no encontrada en la base de datos: " + codigo);
        }
    }

    private void ensureNumeroValido(String numero) {
        if (numero == null || !numero.matches("\\d+")) {
            throw new IllegalArgumentException("Número inválido (debe contener solo dígitos).");
        }
        if (numero.length() > 20) {
            throw new IllegalArgumentException("Número demasiado largo (max 20).");
        }
    }

    private String sanitizeDigits(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    private TelefonoUsuario.TipoTelefono parseTipoUsuario(String tipo) {
        if (tipo == null) return TelefonoUsuario.TipoTelefono.PRINCIPAL;
        return TelefonoUsuario.TipoTelefono.valueOf(tipo.trim().toUpperCase());
    }

    private TelefonoEmpresa.TipoTelefono parseTipoEmpresa(String tipo) {
        if (tipo == null) return TelefonoEmpresa.TipoTelefono.EMPRESA;
        return TelefonoEmpresa.TipoTelefono.valueOf(tipo.trim().toUpperCase());
    }

    private TelefonoResponseDTO toResponseDTO(TelefonoUsuario e) {
        TelefonoResponseDTO r = new TelefonoResponseDTO();
        r.setId(e.getId());
        r.setOwnerType("USUARIO");
        r.setOwnerId(e.getPerfilUsuario() != null ? e.getPerfilUsuario().getId() : null);
        r.setTipo(e.getTipo() != null ? e.getTipo().name() : null);
        r.setNumero(e.getNumero());
        r.setCaracteristica(e.getCaracteristica());
        r.setActivo(e.getActivo());
        r.setVerificado(e.getVerificado());
        r.setFavorito(e.getFavorito());
        return r;
    }

    private TelefonoResponseDTO toResponseDTO(TelefonoEmpresa e) {
        TelefonoResponseDTO r = new TelefonoResponseDTO();
        r.setId(e.getId());
        r.setOwnerType("EMPRESA");
        // idPerfilEmpresa
        try {
            Object id = PerfilEmpresa.class.getMethod("getIdPerfilEmpresa").invoke(e.getPerfilEmpresa());
            r.setOwnerId(id instanceof Long ? (Long) id : null);
        } catch (Exception ignore) {
            try {
                Object id = PerfilEmpresa.class.getMethod("getId").invoke(e.getPerfilEmpresa());
                r.setOwnerId(id instanceof Long ? (Long) id : null);
            } catch (Exception ignored) { r.setOwnerId(null); }
        }
        r.setTipo(e.getTipo() != null ? e.getTipo().name() : null);
        r.setNumero(e.getNumero());
        r.setCaracteristica(e.getCaracteristica());
        r.setActivo(e.getActivo());
        r.setVerificado(e.getVerificado());
        r.setFavorito(null);
        return r;
    }
}
