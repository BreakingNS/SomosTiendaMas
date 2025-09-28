package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ICodigoAreaRepository;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Objects;

@Service
public class TelefonoService implements ITelefonoService {

    @Autowired
    private ITelefonoRepository telefonoRepository;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private IPerfilEmpresaRepository perfilEmpresaRepository;
    @Autowired
    private ICodigoAreaRepository codigoAreaRepository;

    @Override
    public TelefonoResponseDTO registrarTelefono(RegistroTelefonoDTO dto) {
        // Validar que la característica exista en codigos_area
        if (dto.getCaracteristica() == null || !codigoAreaRepository.findByCodigo(dto.getCaracteristica()).isPresent()) {
            throw new IllegalArgumentException("La característica (código de área) no es válida.");
        }

        Telefono telefono = new Telefono();

        // validación XOR: debe venir uno y sólo uno
        boolean hasUsuario = dto.getIdUsuario() != null;
        boolean hasPerfil = dto.getIdPerfilEmpresa() != null;
        if (hasUsuario == hasPerfil) { // ambos true o ambos false
            throw new IllegalArgumentException("Se debe proporcionar exactamente uno de idUsuario o idPerfilEmpresa");
        }

        // ---- NUEVO: asignar owner al telefono usando los ids del DTO ----
        if (hasUsuario) {
            telefono.setUsuario(usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + dto.getIdUsuario())));
        } else {
            telefono.setPerfilEmpresa(perfilEmpresaRepository.findById(dto.getIdPerfilEmpresa())
                .orElseThrow(() -> new EntityNotFoundException("PerfilEmpresa no encontrado: " + dto.getIdPerfilEmpresa())));
        }
        // -----------------------------------------------------------------

        // Campos básicos + defaults
        telefono.setTipo(Telefono.TipoTelefono.valueOf(dto.getTipo()));
        telefono.setNumero(dto.getNumero());
        telefono.setCaracteristica(dto.getCaracteristica());
        telefono.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        telefono.setVerificado(dto.getVerificado() != null ? dto.getVerificado() : false);

        // Buscar existentes del mismo owner
        Long usuarioId = dto.getIdUsuario();
        Long empresaId = dto.getIdPerfilEmpresa();

        List<Telefono> existentes = Collections.emptyList();
        if (usuarioId != null) {
            existentes = telefonoRepository.findByUsuario_IdUsuario(usuarioId);
        } else if (empresaId != null) {
            existentes = telefonoRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresaId);
        }

        java.util.function.Function<String,String> norm = s -> s == null ? "" : s.trim();

        for (Telefono t : existentes) {
            boolean igual =
                Objects.equals(telefono.getTipo(), t.getTipo()) &&
                norm.apply(telefono.getNumero()).equals(norm.apply(t.getNumero())) &&
                norm.apply(telefono.getCaracteristica()).equals(norm.apply(t.getCaracteristica()));
            if (igual) {
                return mapToResponseDTO(t);
            }
        }

        telefono = telefonoRepository.save(telefono);
        return mapToResponseDTO(telefono);
    }

    private TelefonoResponseDTO mapToResponseDTO(Telefono telefono) {
        TelefonoResponseDTO response = new TelefonoResponseDTO();
        response.setIdTelefono(telefono.getIdTelefono());
        response.setTipo(telefono.getTipo() != null ? telefono.getTipo().name() : null);
        response.setNumero(telefono.getNumero());
        response.setCaracteristica(telefono.getCaracteristica());
        response.setActivo(telefono.getActivo());
        response.setVerificado(telefono.getVerificado());
        return response;
    }

    @Override
    public TelefonoResponseDTO actualizarTelefono(Long id, ActualizarTelefonoDTO dto) {
        Telefono telefono = telefonoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Telefono no encontrado: " + id));

        // aplicar cambios solo si vienen no nulos (soporte partial update)
        if (dto.getTipo() != null) {
            telefono.setTipo(Telefono.TipoTelefono.valueOf(dto.getTipo()));
        }
        if (dto.getNumero() != null) {
            telefono.setNumero(dto.getNumero().trim());
        }
        if (dto.getCaracteristica() != null) {
            telefono.setCaracteristica(dto.getCaracteristica().trim());
        }
        if (dto.getActivo() != null) {
            telefono.setActivo(dto.getActivo());
        }
        if (dto.getVerificado() != null) {
            telefono.setVerificado(dto.getVerificado());
        }

        Telefono saved = telefonoRepository.save(telefono);
        return mapToResponseDTO(saved);
    }

    /*
    @Override
    public TelefonoResponseDTO actualizarTelefono(Long id, ActualizarTelefonoDTO dto) {
        Telefono telefono = telefonoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el teléfono con id: " + id));

        telefono.setTipo(Telefono.TipoTelefono.valueOf(dto.getTipo()));
        telefono.setNumero(dto.getNumero());
        telefono.setCaracteristica(dto.getCaracteristica());
        telefono.setActivo(dto.getActivo() != null ? dto.getActivo() : telefono.getActivo());
        telefono.setVerificado(dto.getVerificado() != null ? dto.getVerificado() : telefono.getVerificado());

        telefono = telefonoRepository.save(telefono);

        TelefonoResponseDTO response = new TelefonoResponseDTO();
        response.setIdTelefono(telefono.getIdTelefono());
        response.setTipo(telefono.getTipo().name());
        response.setNumero(telefono.getNumero());
        response.setCaracteristica(telefono.getCaracteristica());
        response.setActivo(telefono.getActivo());
        response.setVerificado(telefono.getVerificado());

        return response;
    }*/

    @Override
    public TelefonoResponseDTO obtenerTelefono(Long id) {
        Telefono telefono = telefonoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("No se encontró el teléfono con id: " + id));

        TelefonoResponseDTO response = new TelefonoResponseDTO();
        response.setIdTelefono(telefono.getIdTelefono());
        response.setTipo(telefono.getTipo().name());
        response.setNumero(telefono.getNumero());
        response.setCaracteristica(telefono.getCaracteristica());
        response.setActivo(telefono.getActivo());
        response.setVerificado(telefono.getVerificado());

        return response;
    }

    @Override
    public List<TelefonoResponseDTO> listarTelefonosPorUsuario(Long idUsuario) {
        List<Telefono> telefonos = telefonoRepository.findByUsuario_IdUsuario(idUsuario);
        return telefonos.stream().map(telefono -> {
            TelefonoResponseDTO response = new TelefonoResponseDTO();
            response.setIdTelefono(telefono.getIdTelefono());
            response.setTipo(telefono.getTipo().name());
            response.setNumero(telefono.getNumero());
            response.setCaracteristica(telefono.getCaracteristica());
            response.setActivo(telefono.getActivo());
            response.setVerificado(telefono.getVerificado());

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TelefonoResponseDTO> listarTelefonosPorPerfilEmpresa(Long idPerfilEmpresa) {
        List<Telefono> telefonos = telefonoRepository.findByPerfilEmpresa_IdPerfilEmpresa(idPerfilEmpresa);
        return telefonos.stream().map(telefono -> {
            TelefonoResponseDTO response = new TelefonoResponseDTO();
            response.setIdTelefono(telefono.getIdTelefono());
            response.setTipo(telefono.getTipo().name());
            response.setNumero(telefono.getNumero());
            response.setCaracteristica(telefono.getCaracteristica());
            response.setActivo(telefono.getActivo());
            response.setVerificado(telefono.getVerificado());

            return response;
        }).collect(Collectors.toList());
    }

    public List<Telefono> traerTodoTelefono() {
        return telefonoRepository.findAll();
    }

    public void eliminarTelefonosPorUsuario(Long idUsuario) {
        List<Telefono> telefonos = telefonoRepository.findByUsuario_IdUsuario(idUsuario);
        telefonoRepository.deleteAll(telefonos);
    }

    public void eliminarTelefonosPorPerfilEmpresa(Long idPerfilEmpresa) {
        List<Telefono> telefonos = telefonoRepository.findByPerfilEmpresa_IdPerfilEmpresa(idPerfilEmpresa);
        telefonoRepository.deleteAll(telefonos);
    }

}
