package com.breakingns.SomosTiendaMas.entidades.direccion.service;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.Direccion;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.IDireccionRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

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
            direccion.setUsuario(usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + dto.getIdUsuario())));
        }
        if (dto.getIdPerfilEmpresa() != null) {
            direccion.setPerfilEmpresa(perfilEmpresaRepository.findById(dto.getIdPerfilEmpresa())
                .orElseThrow(() -> new EntityNotFoundException("PerfilEmpresa no encontrado: " + dto.getIdPerfilEmpresa())));
        }

        // Asignar entidades de ubicación
        if (dto.getIdPais() != null) {
            direccion.setPais(paisRepository.findById(dto.getIdPais())
                .orElseThrow(() -> new EntityNotFoundException("País no encontrado: " + dto.getIdPais())));
        }
        if (dto.getIdProvincia() != null) {
            direccion.setProvincia(provinciaRepository.findById(dto.getIdProvincia())
                .orElseThrow(() -> new EntityNotFoundException("Provincia no encontrada: " + dto.getIdProvincia())));
        }
        if (dto.getIdDepartamento() != null) {
            direccion.setDepartamento(departamentoRepository.findById(dto.getIdDepartamento())
                .orElseThrow(() -> new EntityNotFoundException("Departamento no encontrado: " + dto.getIdDepartamento())));
        }
        if (dto.getIdLocalidad() != null) {
            direccion.setLocalidad(localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada: " + dto.getIdLocalidad())));
        }
        if (dto.getIdMunicipio() != null) {
            direccion.setMunicipio(municipioRepository.findById(dto.getIdMunicipio())
                .orElseThrow(() -> new EntityNotFoundException("Municipio no encontrado: " + dto.getIdMunicipio())));
        }

        // Otros campos (mantener defaults)
        direccion.setTipo(Direccion.TipoDireccion.valueOf(dto.getTipo()));
        direccion.setCalle(dto.getCalle());
        direccion.setNumero(dto.getNumero());
        direccion.setPiso(dto.getPiso());
        direccion.setReferencia(dto.getReferencia());
        direccion.setActiva(dto.getActiva() != null ? dto.getActiva() : true);
        direccion.setEsPrincipal(dto.getEsPrincipal() != null ? dto.getEsPrincipal() : false);
        direccion.setCodigoPostal(dto.getCodigoPostal());

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

    /*
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
    */
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
        Direccion existente = direccionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada: " + id));

        // Actualizar entidades de ubicación sólo si vienen en DTO
        if (dto.getIdPais() != null) {
            existente.setPais(paisRepository.findById(dto.getIdPais())
                .orElseThrow(() -> new EntityNotFoundException("País no encontrado: " + dto.getIdPais())));
        }
        if (dto.getIdProvincia() != null) {
            existente.setProvincia(provinciaRepository.findById(dto.getIdProvincia())
                .orElseThrow(() -> new EntityNotFoundException("Provincia no encontrada: " + dto.getIdProvincia())));
        }
        if (dto.getIdDepartamento() != null) {
            existente.setDepartamento(departamentoRepository.findById(dto.getIdDepartamento())
                .orElseThrow(() -> new EntityNotFoundException("Departamento no encontrado: " + dto.getIdDepartamento())));
        }
        if (dto.getIdLocalidad() != null) {
            existente.setLocalidad(localidadRepository.findById(dto.getIdLocalidad())
                .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada: " + dto.getIdLocalidad())));
        }
        if (dto.getIdMunicipio() != null) {
            existente.setMunicipio(municipioRepository.findById(dto.getIdMunicipio())
                .orElseThrow(() -> new EntityNotFoundException("Municipio no encontrado: " + dto.getIdMunicipio())));
        }

        // Campos simples: si vienen, actualizarlos; si no, mantener los existentes
        if (dto.getTipo() != null) {
            try {
                existente.setTipo(Direccion.TipoDireccion.valueOf(dto.getTipo()));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Tipo de dirección inválido: " + dto.getTipo());
            }
        }
        if (dto.getCalle() != null) existente.setCalle(dto.getCalle());
        if (dto.getNumero() != null) existente.setNumero(dto.getNumero());
        if (dto.getPiso() != null) existente.setPiso(dto.getPiso());
        if (dto.getReferencia() != null) existente.setReferencia(dto.getReferencia());
        if (dto.getActiva() != null) existente.setActiva(dto.getActiva());
        if (dto.getEsPrincipal() != null) existente.setEsPrincipal(dto.getEsPrincipal());
        if (dto.getCodigoPostal() != null) existente.setCodigoPostal(dto.getCodigoPostal());

        // Lógica de esPrincipal: si se marca true, desmarcar otras del mismo owner
        Long usuarioId = existente.getUsuario() != null ? existente.getUsuario().getIdUsuario() : null;
        Long empresaId = existente.getPerfilEmpresa() != null ? existente.getPerfilEmpresa().getIdPerfilEmpresa() : null;
        if (Boolean.TRUE.equals(existente.getEsPrincipal())) {
            if (usuarioId != null) {
                direccionRepository.findByUsuario_IdUsuario(usuarioId)
                    .stream()
                    .filter(d -> !d.getIdDireccion().equals(existente.getIdDireccion()))
                    .filter(d -> Boolean.TRUE.equals(d.getEsPrincipal()))
                    .forEach(d -> { d.setEsPrincipal(false); direccionRepository.save(d); });
            } else if (empresaId != null) {
                direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(empresaId)
                    .stream()
                    .filter(d -> !d.getIdDireccion().equals(existente.getIdDireccion()))
                    .filter(d -> Boolean.TRUE.equals(d.getEsPrincipal()))
                    .forEach(d -> { d.setEsPrincipal(false); direccionRepository.save(d); });
            }
        }

        Direccion guardada = direccionRepository.save(existente);
        return mapToResponseDTO(guardada);
    }

    @Override
    public DireccionResponseDTO obtenerDireccion(Long id) {
        Direccion d = direccionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada: " + id));
        return mapToResponseDTO(d);
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorUsuario(Long idUsuario) {
        List<Direccion> lista = direccionRepository.findByUsuario_IdUsuario(idUsuario);
        return lista.stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorPerfilEmpresa(Long idPerfilEmpresa) {
        List<Direccion> lista = direccionRepository.findByPerfilEmpresa_IdPerfilEmpresa(idPerfilEmpresa);
        return lista.stream().map(this::mapToResponseDTO).toList();
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