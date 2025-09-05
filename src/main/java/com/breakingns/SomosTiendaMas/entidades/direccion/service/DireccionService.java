package com.breakingns.SomosTiendaMas.entidades.direccion.service;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DireccionService implements IDireccionService {

    @Autowired
    private IDireccionRepository direccionRepository;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private IPerfilEmpresaRepository perfilEmpresaRepository;

    @Override
    public DireccionResponseDTO registrarDireccion(RegistroDireccionDTO dto) {
        Direccion direccion = new Direccion();

        // Asignar usuario si corresponde
        if (dto.getIdUsuario() != null) {
            direccion.setUsuario(usuarioRepository.findById(dto.getIdUsuario()).orElse(null));
        }
        // Asignar perfil empresa si corresponde
        if (dto.getIdPerfilEmpresa() != null) {
            direccion.setPerfilEmpresa(perfilEmpresaRepository.findById(dto.getIdPerfilEmpresa()).orElse(null));
        }

        direccion.setTipo(Direccion.TipoDireccion.valueOf(dto.getTipo()));
        direccion.setCalle(dto.getCalle());
        direccion.setNumero(dto.getNumero());
        direccion.setPiso(dto.getPiso());
        direccion.setDepartamento(dto.getDepartamento());
        direccion.setCiudad(dto.getCiudad());
        direccion.setProvincia(dto.getProvincia());
        direccion.setCodigoPostal(dto.getCodigoPostal());
        direccion.setPais(dto.getPais());
        direccion.setReferencia(dto.getReferencia());
        direccion.setActiva(dto.getActiva() != null ? dto.getActiva() : true);
        direccion.setEsPrincipal(dto.getEsPrincipal() != null ? dto.getEsPrincipal() : false);

        // Asignar dirección copiada si corresponde
        if (dto.getEsCopiaDe() != null) {
            direccion.setDireccionCopiada(direccionRepository.findById(dto.getEsCopiaDe()).orElse(null));
        }

        // Guardar en la base de datos
        direccion = direccionRepository.save(direccion);

        // Mapear a DireccionResponseDTO
        DireccionResponseDTO response = new DireccionResponseDTO();
        response.setIdDireccion(direccion.getIdDireccion());
        response.setTipo(direccion.getTipo().name());
        response.setCalle(direccion.getCalle());
        response.setNumero(direccion.getNumero());
        response.setPiso(direccion.getPiso());
        response.setDepartamento(direccion.getDepartamento());
        response.setCiudad(direccion.getCiudad());
        response.setProvincia(direccion.getProvincia());
        response.setCodigoPostal(direccion.getCodigoPostal());
        response.setPais(direccion.getPais());
        response.setReferencia(direccion.getReferencia());
        response.setActiva(direccion.getActiva());
        response.setEsPrincipal(direccion.getEsPrincipal());
        response.setEsCopiaDe(direccion.getDireccionCopiada() != null ? direccion.getDireccionCopiada().getIdDireccion() : null);

        return response;
    }

    @Override
    public DireccionResponseDTO actualizarDireccion(Long id, ActualizarDireccionDTO dto) {
        Direccion direccion = direccionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la dirección con id: " + id));

        if (dto.getTipo() != null) direccion.setTipo(Direccion.TipoDireccion.valueOf(dto.getTipo()));
        if (dto.getCalle() != null) direccion.setCalle(dto.getCalle());
        if (dto.getNumero() != null) direccion.setNumero(dto.getNumero());
        if (dto.getPiso() != null) direccion.setPiso(dto.getPiso());
        if (dto.getDepartamento() != null) direccion.setDepartamento(dto.getDepartamento());
        if (dto.getCiudad() != null) direccion.setCiudad(dto.getCiudad());
        if (dto.getProvincia() != null) direccion.setProvincia(dto.getProvincia());
        if (dto.getCodigoPostal() != null) direccion.setCodigoPostal(dto.getCodigoPostal());
        if (dto.getPais() != null) direccion.setPais(dto.getPais());
        if (dto.getReferencia() != null) direccion.setReferencia(dto.getReferencia());
        if (dto.getActiva() != null) direccion.setActiva(dto.getActiva());
        if (dto.getEsPrincipal() != null) direccion.setEsPrincipal(dto.getEsPrincipal());
        if (dto.getEsCopiaDe() != null) {
            direccion.setDireccionCopiada(direccionRepository.findById(dto.getEsCopiaDe()).orElse(null));
        }

        direccion = direccionRepository.save(direccion);
        return mapToResponseDTO(direccion);
    }

    public void eliminarDireccionesPorUsuario(Long idUsuario) {
        List<Direccion> direcciones = direccionRepository.findByUsuario_IdUsuario(idUsuario);
        direccionRepository.deleteAll(direcciones);
    }

    public void eliminarDireccionesPorPerfilEmpresa(Long idPerfilEmpresa) {
        List<Direccion> direcciones = direccionRepository.findByPerfilEmpresa_Id(idPerfilEmpresa);
        direccionRepository.deleteAll(direcciones);
    }

    // Método privado para mapear Direccion a DireccionResponseDTO
    private DireccionResponseDTO mapToResponseDTO(Direccion direccion) {
        DireccionResponseDTO response = new DireccionResponseDTO();
        response.setIdDireccion(direccion.getIdDireccion());
        response.setTipo(direccion.getTipo() != null ? direccion.getTipo().name() : null);
        response.setCalle(direccion.getCalle());
        response.setNumero(direccion.getNumero());
        response.setPiso(direccion.getPiso());
        response.setDepartamento(direccion.getDepartamento());
        response.setCiudad(direccion.getCiudad());
        response.setProvincia(direccion.getProvincia());
        response.setCodigoPostal(direccion.getCodigoPostal());
        response.setPais(direccion.getPais());
        response.setReferencia(direccion.getReferencia());
        response.setActiva(direccion.getActiva());
        response.setEsPrincipal(direccion.getEsPrincipal());
        response.setEsCopiaDe(direccion.getDireccionCopiada() != null ? direccion.getDireccionCopiada().getIdDireccion() : null);
        return response;
    }

    @Override
    public DireccionResponseDTO obtenerDireccion(Long id) {
        Direccion direccion = direccionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la dirección con id: " + id));

        DireccionResponseDTO response = new DireccionResponseDTO();
        response.setIdDireccion(direccion.getIdDireccion());
        response.setTipo(direccion.getTipo().name());
        response.setCalle(direccion.getCalle());
        response.setNumero(direccion.getNumero());
        response.setPiso(direccion.getPiso());
        response.setDepartamento(direccion.getDepartamento());
        response.setCiudad(direccion.getCiudad());
        response.setProvincia(direccion.getProvincia());
        response.setCodigoPostal(direccion.getCodigoPostal());
        response.setPais(direccion.getPais());
        response.setReferencia(direccion.getReferencia());
        response.setActiva(direccion.getActiva());
        response.setEsPrincipal(direccion.getEsPrincipal());
        response.setEsCopiaDe(direccion.getDireccionCopiada() != null ? direccion.getDireccionCopiada().getIdDireccion() : null);

        return response;
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorUsuario(Long idUsuario) {
        List<Direccion> direcciones = direccionRepository.findByUsuario_IdUsuario(idUsuario);
        return direcciones.stream().map(direccion -> {
            DireccionResponseDTO response = new DireccionResponseDTO();
            response.setIdDireccion(direccion.getIdDireccion());
            response.setTipo(direccion.getTipo().name());
            response.setCalle(direccion.getCalle());
            response.setNumero(direccion.getNumero());
            response.setPiso(direccion.getPiso());
            response.setDepartamento(direccion.getDepartamento());
            response.setCiudad(direccion.getCiudad());
            response.setProvincia(direccion.getProvincia());
            response.setCodigoPostal(direccion.getCodigoPostal());
            response.setPais(direccion.getPais());
            response.setReferencia(direccion.getReferencia());
            response.setActiva(direccion.getActiva());
            response.setEsPrincipal(direccion.getEsPrincipal());
            response.setEsCopiaDe(direccion.getDireccionCopiada() != null ? direccion.getDireccionCopiada().getIdDireccion() : null);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorPerfilEmpresa(Long idPerfilEmpresa) {
        List<Direccion> direcciones = direccionRepository.findByPerfilEmpresa_Id(idPerfilEmpresa);
        return direcciones.stream().map(direccion -> {
            DireccionResponseDTO response = new DireccionResponseDTO();
            response.setIdDireccion(direccion.getIdDireccion());
            response.setTipo(direccion.getTipo().name());
            response.setCalle(direccion.getCalle());
            response.setNumero(direccion.getNumero());
            response.setPiso(direccion.getPiso());
            response.setDepartamento(direccion.getDepartamento());
            response.setCiudad(direccion.getCiudad());
            response.setProvincia(direccion.getProvincia());
            response.setCodigoPostal(direccion.getCodigoPostal());
            response.setPais(direccion.getPais());
            response.setReferencia(direccion.getReferencia());
            response.setActiva(direccion.getActiva());
            response.setEsPrincipal(direccion.getEsPrincipal());
            response.setEsCopiaDe(direccion.getDireccionCopiada() != null ? direccion.getDireccionCopiada().getIdDireccion() : null);
            return response;
        }).collect(Collectors.toList());
    }

    public List<Direccion> traerTodoDireccion() {
        return direccionRepository.findAll();
    }
}
