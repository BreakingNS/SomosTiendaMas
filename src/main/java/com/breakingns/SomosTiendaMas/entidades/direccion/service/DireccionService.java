package com.breakingns.SomosTiendaMas.entidades.direccion.service;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IPaisRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IProvinciaRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IDepartamentoRepository;
import com.breakingns.SomosTiendaMas.auth.repository.ILocalidadRepository;
import com.breakingns.SomosTiendaMas.auth.repository.IMunicipioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Collections;
import java.util.Objects;

@Service
public class DireccionService implements IDireccionService {

    @Autowired
    private IDireccionRepository direccionRepository;
    @Autowired
    private IUsuarioRepository usuarioRepository;
    @Autowired
    private IPerfilEmpresaRepository perfilEmpresaRepository;
    @Autowired
    private IPaisRepository paisRepository;
    @Autowired
    private IProvinciaRepository provinciaRepository;
    @Autowired
    private IDepartamentoRepository departamentoRepository;
    @Autowired
    private ILocalidadRepository localidadRepository;
    @Autowired
    private IMunicipioRepository municipioRepository;

    @Override
    public DireccionResponseDTO registrarDireccion(RegistroDireccionDTO dto) {
        Direccion direccion = new Direccion();

        // Asignar usuario o empresa
        if (dto.getIdUsuario() != null) {
            direccion.setUsuario(usuarioRepository.findById(dto.getIdUsuario()).orElse(null));
        }
        if (dto.getIdPerfilEmpresa() != null) {
            direccion.setPerfilEmpresa(perfilEmpresaRepository.findById(dto.getIdPerfilEmpresa()).orElse(null));
        }

        // Asignar entidades de ubicación
        direccion.setPais(dto.getIdPais() != null ? paisRepository.findById(dto.getIdPais()).orElse(null) : null);
        direccion.setProvincia(dto.getIdProvincia() != null ? provinciaRepository.findById(dto.getIdProvincia()).orElse(null) : null);
        direccion.setDepartamento(dto.getIdDepartamento() != null ? departamentoRepository.findById(dto.getIdDepartamento()).orElse(null) : null);
        direccion.setLocalidad(dto.getIdLocalidad() != null ? localidadRepository.findById(dto.getIdLocalidad()).orElse(null) : null);
        direccion.setMunicipio(dto.getIdMunicipio() != null ? municipioRepository.findById(dto.getIdMunicipio()).orElse(null) : null);

        // Otros campos (mantener defaults)
        direccion.setTipo(Direccion.TipoDireccion.valueOf(dto.getTipo()));
        direccion.setCalle(dto.getCalle());
        direccion.setNumero(dto.getNumero());
        direccion.setPiso(dto.getPiso());
        direccion.setReferencia(dto.getReferencia());
        direccion.setActiva(dto.getActiva() != null ? dto.getActiva() : true);
        direccion.setEsPrincipal(dto.getEsPrincipal() != null ? dto.getEsPrincipal() : false);
        direccion.setCodigoPostal(dto.getCodigoPostal());
        //direccion.setUsarComoEnvio(dto.getUsarComoEnvio() != null ? dto.getUsarComoEnvio() : false);

        // Buscar direcciones existentes del mismo owner (usuario o empresa)
        Long usuarioId = direccion.getUsuario() != null ? direccion.getUsuario().getIdUsuario() : null;
        Long empresaId = direccion.getPerfilEmpresa() != null ? direccion.getPerfilEmpresa().getIdPerfilEmpresa() : null;

        List<Direccion> existentes = Collections.emptyList();
        if (usuarioId != null) {
            existentes = direccionRepository.findByUsuario_IdUsuario(usuarioId);
        } else if (empresaId != null) {
            existentes = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresaId);
        }

        // Normalizador simple
        java.util.function.Function<String, String> norm = s -> {
            if (s == null) return "";
            String n = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "") // quita acentos
                    .replaceAll("\\s+", " ");
            return n;
        };

        // Comparar con existentes
        for (Direccion e : existentes) {
            boolean igual =
                Objects.equals(direccion.getTipo(), e.getTipo()) &&
                Objects.equals(direccion.getPais() != null ? direccion.getPais().getId() : null, e.getPais() != null ? e.getPais().getId() : null) &&
                Objects.equals(direccion.getProvincia() != null ? direccion.getProvincia().getId() : null, e.getProvincia() != null ? e.getProvincia().getId() : null) &&
                Objects.equals(direccion.getDepartamento() != null ? direccion.getDepartamento().getId() : null, e.getDepartamento() != null ? e.getDepartamento().getId() : null) &&
                Objects.equals(direccion.getLocalidad() != null ? direccion.getLocalidad().getId() : null, e.getLocalidad() != null ? e.getLocalidad().getId() : null) &&
                norm.apply(direccion.getCalle()).equals(norm.apply(e.getCalle())) &&
                Objects.equals(direccion.getNumero(), e.getNumero()) &&
                Objects.equals(direccion.getPiso(), e.getPiso()) &&
                Objects.equals(direccion.getCodigoPostal(), e.getCodigoPostal());

            if (igual) {
                // Reutilizar existente (no crear duplicado)
                return mapToResponseDTO(e);
            }
        }

        // Si es principal, desmarcar otras principales del mismo owner
        if (Boolean.TRUE.equals(direccion.getEsPrincipal())) {
            if (usuarioId != null) {
                direccionRepository.findByUsuario_IdUsuario(usuarioId)
                    .stream()
                    .filter(d -> Boolean.TRUE.equals(d.getEsPrincipal()))
                    .forEach(d -> { d.setEsPrincipal(false); direccionRepository.save(d); });
            } else if (empresaId != null) {
                direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresaId)
                    .stream()
                    .filter(d -> Boolean.TRUE.equals(d.getEsPrincipal()))
                    .forEach(d -> { d.setEsPrincipal(false); direccionRepository.save(d); });
            }
        }

        // Guardar en la base de datos
        Direccion guardada = direccionRepository.save(direccion);

        return mapToResponseDTO(guardada);
    }

    // El resto de los métodos (actualizar, obtener, listar) deben mapear las entidades de ubicación a sus IDs o nombres en el DTO de respuesta según lo que necesites mostrar.

    // Ejemplo de mapeo para el response DTO:
    private DireccionResponseDTO mapToResponseDTO(Direccion direccion) {
        DireccionResponseDTO response = new DireccionResponseDTO();
        response.setIdDireccion(direccion.getIdDireccion());
        response.setTipo(direccion.getTipo() != null ? direccion.getTipo().name() : null);
        response.setCalle(direccion.getCalle());
        response.setNumero(direccion.getNumero());
        response.setPiso(direccion.getPiso());
        response.setReferencia(direccion.getReferencia());
        response.setActiva(direccion.getActiva());
        response.setEsPrincipal(direccion.getEsPrincipal());
        response.setCodigoPostal(direccion.getCodigoPostal());

        // Entidades de ubicación
        response.setIdPais(direccion.getPais() != null ? direccion.getPais().getId() : null);
        response.setIdProvincia(direccion.getProvincia() != null ? direccion.getProvincia().getId() : null);
        response.setIdDepartamento(direccion.getDepartamento() != null ? direccion.getDepartamento().getId() : null);
        response.setIdLocalidad(direccion.getLocalidad() != null ? direccion.getLocalidad().getId() : null);
        response.setIdMunicipio(direccion.getMunicipio() != null ? direccion.getMunicipio().getId() : null);

        return response;
    }

    @Override
    public DireccionResponseDTO actualizarDireccion(Long id, ActualizarDireccionDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizarDireccion'");
    }

    @Override
    public DireccionResponseDTO obtenerDireccion(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerDireccion'");
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorUsuario(Long idUsuario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarDireccionesPorUsuario'");
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorPerfilEmpresa(Long idPerfilEmpresa) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarDireccionesPorPerfilEmpresa'");
    }

    // Implementa los otros métodos de la interfaz siguiendo la misma lógica de mapeo y persistencia.

    @Override
    public void eliminarDireccionesPorUsuario(Long idUsuario) {
        direccionRepository.deleteAllByUsuario_IdUsuario(idUsuario);
    }

    @Override
    public void eliminarDireccionesPorPerfilEmpresa(Long idPerfilEmpresa) {
        direccionRepository.deleteAllByPerfilEmpresa_IdPerfilEmpresa(idPerfilEmpresa);
    }

    @Override
    public List<Direccion> traerTodoDireccion() {
        return direccionRepository.findAll();
    }
}