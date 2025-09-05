package com.breakingns.SomosTiendaMas.entidades.telefono.service;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.RegistroTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.ActualizarTelefonoDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.dto.TelefonoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.model.Telefono;
import com.breakingns.SomosTiendaMas.entidades.telefono.repository.ITelefonoRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TelefonoService implements ITelefonoService {

    @Autowired
    private ITelefonoRepository telefonoRepository;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private IPerfilEmpresaRepository perfilEmpresaRepository;

    @Override
    public TelefonoResponseDTO registrarTelefono(RegistroTelefonoDTO dto) {
        Telefono telefono = new Telefono();

        if (dto.getIdUsuario() != null) {
            telefono.setUsuario(usuarioRepository.findById(dto.getIdUsuario()).orElse(null));
        }
        if (dto.getIdPerfilEmpresa() != null) {
            telefono.setPerfilEmpresa(perfilEmpresaRepository.findById(dto.getIdPerfilEmpresa()).orElse(null));
        }

        telefono.setTipo(Telefono.TipoTelefono.valueOf(dto.getTipo()));
        telefono.setNumero(dto.getNumero());
        telefono.setCaracteristica(dto.getCaracteristica());
        telefono.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        telefono.setVerificado(dto.getVerificado() != null ? dto.getVerificado() : false);

        if (dto.getEsCopiaDe() != null) {
            telefono.setTelefonoCopiado(telefonoRepository.findById(dto.getEsCopiaDe()).orElse(null));
        }

        telefono = telefonoRepository.save(telefono);

        TelefonoResponseDTO response = new TelefonoResponseDTO();
        response.setIdTelefono(telefono.getIdTelefono());
        response.setTipo(telefono.getTipo().name());
        response.setNumero(telefono.getNumero());
        response.setCaracteristica(telefono.getCaracteristica());
        response.setActivo(telefono.getActivo());
        response.setVerificado(telefono.getVerificado());
        response.setEsCopiaDe(telefono.getTelefonoCopiado() != null ? telefono.getTelefonoCopiado().getIdTelefono() : null);

        return response;
    }

    @Override
    public TelefonoResponseDTO actualizarTelefono(Long id, ActualizarTelefonoDTO dto) {
        Telefono telefono = telefonoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el teléfono con id: " + id));

        telefono.setTipo(Telefono.TipoTelefono.valueOf(dto.getTipo()));
        telefono.setNumero(dto.getNumero());
        telefono.setCaracteristica(dto.getCaracteristica());
        telefono.setActivo(dto.getActivo() != null ? dto.getActivo() : telefono.getActivo());
        telefono.setVerificado(dto.getVerificado() != null ? dto.getVerificado() : telefono.getVerificado());
        if (dto.getEsCopiaDe() != null) {
            telefono.setTelefonoCopiado(telefonoRepository.findById(dto.getEsCopiaDe()).orElse(null));
        }

        telefono = telefonoRepository.save(telefono);

        TelefonoResponseDTO response = new TelefonoResponseDTO();
        response.setIdTelefono(telefono.getIdTelefono());
        response.setTipo(telefono.getTipo().name());
        response.setNumero(telefono.getNumero());
        response.setCaracteristica(telefono.getCaracteristica());
        response.setActivo(telefono.getActivo());
        response.setVerificado(telefono.getVerificado());
        response.setEsCopiaDe(telefono.getTelefonoCopiado() != null ? telefono.getTelefonoCopiado().getIdTelefono() : null);

        return response;
    }

    @Override
    public TelefonoResponseDTO obtenerTelefono(Long id) {
        Telefono telefono = telefonoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el teléfono con id: " + id));

        TelefonoResponseDTO response = new TelefonoResponseDTO();
        response.setIdTelefono(telefono.getIdTelefono());
        response.setTipo(telefono.getTipo().name());
        response.setNumero(telefono.getNumero());
        response.setCaracteristica(telefono.getCaracteristica());
        response.setActivo(telefono.getActivo());
        response.setVerificado(telefono.getVerificado());
        response.setEsCopiaDe(telefono.getTelefonoCopiado() != null ? telefono.getTelefonoCopiado().getIdTelefono() : null);

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
            response.setEsCopiaDe(telefono.getTelefonoCopiado() != null ? telefono.getTelefonoCopiado().getIdTelefono() : null);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TelefonoResponseDTO> listarTelefonosPorPerfilEmpresa(Long idPerfilEmpresa) {
        List<Telefono> telefonos = telefonoRepository.findByPerfilEmpresa_Id(idPerfilEmpresa);
        return telefonos.stream().map(telefono -> {
            TelefonoResponseDTO response = new TelefonoResponseDTO();
            response.setIdTelefono(telefono.getIdTelefono());
            response.setTipo(telefono.getTipo().name());
            response.setNumero(telefono.getNumero());
            response.setCaracteristica(telefono.getCaracteristica());
            response.setActivo(telefono.getActivo());
            response.setVerificado(telefono.getVerificado());
            response.setEsCopiaDe(telefono.getTelefonoCopiado() != null ? telefono.getTelefonoCopiado().getIdTelefono() : null);
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
        List<Telefono> telefonos = telefonoRepository.findByPerfilEmpresa_Id(idPerfilEmpresa);
        telefonoRepository.deleteAll(telefonos);
    }

}
