package com.breakingns.SomosTiendaMas.entidades.direccion.service;

import com.breakingns.SomosTiendaMas.entidades.direccion.dto.RegistroDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.ActualizarDireccionDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.dto.DireccionResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.DireccionUsuario;
import com.breakingns.SomosTiendaMas.entidades.direccion.model.DireccionEmpresa;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.DireccionUsuarioRepository;
import com.breakingns.SomosTiendaMas.entidades.direccion.repository.DireccionEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.perfil_usuario.model.PerfilUsuario;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.auth.model.Pais;
import com.breakingns.SomosTiendaMas.auth.model.Provincia;
import com.breakingns.SomosTiendaMas.auth.model.Departamento;
import com.breakingns.SomosTiendaMas.auth.model.Localidad;
import com.breakingns.SomosTiendaMas.auth.model.Municipio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class DireccionServiceImpl implements IDireccionService {

    @Autowired
    private DireccionUsuarioRepository usuarioRepo;

    @Autowired
    private DireccionEmpresaRepository empresaRepo;

    @Override
    public DireccionResponseDTO registrarDireccion(RegistroDireccionDTO dto) {
        if (dto.getPerfilUsuarioId() != null) {
            DireccionUsuario e = new DireccionUsuario();
            PerfilUsuario perfil = new PerfilUsuario();
            perfil.setId(dto.getPerfilUsuarioId());
            e.setPerfilUsuario(perfil);
            // Tipo
            if (dto.getTipo() != null) {
                e.setTipo(DireccionUsuario.TipoDireccion.valueOf(dto.getTipo().toUpperCase()));
            }
            // Relaciones (solo setear id para que JPA lo persista como referencia)
            Pais pais = new Pais(); pais.setId(dto.getPaisId()); e.setPais(pais);
            Provincia prov = new Provincia(); prov.setId(dto.getProvinciaId()); e.setProvincia(prov);
            Departamento dep = new Departamento(); dep.setId(dto.getDepartamentoId()); e.setDepartamento(dep);
            Localidad loc = new Localidad(); loc.setId(dto.getLocalidadId()); e.setLocalidad(loc);
            Municipio mun = new Municipio(); mun.setId(dto.getMunicipioId()); e.setMunicipio(mun);

            e.setCalle(dto.getCalle());
            e.setNumero(dto.getNumero());
            e.setPiso(dto.getPiso());
            e.setDepartamentoInterno(dto.getDepartamentoInterno());
            e.setCodigoPostal(dto.getCodigoPostal());
            e.setReferencia(dto.getReferencia());
            e.setActiva(dto.getActiva());
            e.setEsPrincipal(dto.getEsPrincipal());

            DireccionUsuario saved = usuarioRepo.save(e);
            return toResponseDto(saved);
        } else if (dto.getPerfilEmpresaId() != null) {
            DireccionEmpresa e = new DireccionEmpresa();
            PerfilEmpresa perfil = new PerfilEmpresa();
            perfil.setIdPerfilEmpresa(dto.getPerfilEmpresaId());
            e.setPerfilEmpresa(perfil);
            if (dto.getTipo() != null) {
                e.setTipo(DireccionEmpresa.TipoDireccion.valueOf(dto.getTipo().toUpperCase()));
            }
            Pais pais = new Pais(); pais.setId(dto.getPaisId()); e.setPais(pais);
            Provincia prov = new Provincia(); prov.setId(dto.getProvinciaId()); e.setProvincia(prov);
            Departamento dep = new Departamento(); dep.setId(dto.getDepartamentoId()); e.setDepartamento(dep);
            Localidad loc = new Localidad(); loc.setId(dto.getLocalidadId()); e.setLocalidad(loc);
            Municipio mun = new Municipio(); mun.setId(dto.getMunicipioId()); e.setMunicipio(mun);

            e.setCalle(dto.getCalle());
            e.setNumero(dto.getNumero());
            e.setPiso(dto.getPiso());
            e.setDepartamentoInterno(dto.getDepartamentoInterno());
            e.setCodigoPostal(dto.getCodigoPostal());
            e.setReferencia(dto.getReferencia());
            e.setActiva(dto.getActiva());
            e.setEsPrincipal(dto.getEsPrincipal());

            DireccionEmpresa saved = empresaRepo.save(e);
            return toResponseDto(saved);
        } else {
            throw new IllegalArgumentException("Se requiere perfilUsuarioId o perfilEmpresaId");
        }
    }

    @Override
    public DireccionResponseDTO actualizarDireccion(Long id, ActualizarDireccionDTO dto) {
        // Intentar usuario
        Optional<DireccionUsuario> uOpt = usuarioRepo.findById(id);
        if (uOpt.isPresent()) {
            DireccionUsuario e = uOpt.get();
            applyUpdatesToUsuario(e, dto);
            DireccionUsuario saved = usuarioRepo.save(e);
            return toResponseDto(saved);
        }
        // Intentar empresa
        Optional<DireccionEmpresa> empOpt = empresaRepo.findById(id);
        if (empOpt.isPresent()) {
            DireccionEmpresa e = empOpt.get();
            applyUpdatesToEmpresa(e, dto);
            DireccionEmpresa saved = empresaRepo.save(e);
            return toResponseDto(saved);
        }
        throw new IllegalArgumentException("Dirección no encontrada con id: " + id);
    }

    @Override
    public DireccionResponseDTO obtenerDireccion(Long id) {
        Optional<DireccionUsuario> uOpt = usuarioRepo.findById(id);
        if (uOpt.isPresent()) return toResponseDto(uOpt.get());
        Optional<DireccionEmpresa> eOpt = empresaRepo.findById(id);
        if (eOpt.isPresent()) return toResponseDto(eOpt.get());
        throw new IllegalArgumentException("Dirección no encontrada con id: " + id);
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorUsuario(Long perfilUsuarioId) {
        return usuarioRepo.findByPerfilUsuarioId(perfilUsuarioId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DireccionResponseDTO> listarDireccionesPorPerfilEmpresa(Long perfilEmpresaId) {
        return empresaRepo.findByPerfilEmpresaIdPerfilEmpresa(perfilEmpresaId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // Nuevo: eliminar direcciones por perfil de usuario
    public void eliminarDireccionesPorUsuario(Long perfilUsuarioId) {
        usuarioRepo.deleteByPerfilUsuarioId(perfilUsuarioId);
    }

    // Nuevo: eliminar direcciones por perfil de empresa
    public void eliminarDireccionesPorPerfilEmpresa(Long perfilEmpresaId) {
        empresaRepo.deleteByPerfilEmpresaIdPerfilEmpresa(perfilEmpresaId);
    }

    // Helpers

    private DireccionResponseDTO toResponseDto(DireccionUsuario e) {
        DireccionResponseDTO r = new DireccionResponseDTO();
        r.setId(e.getId());
        r.setOwnerType("USUARIO");
        r.setOwnerId(e.getPerfilUsuario() != null ? e.getPerfilUsuario().getId() : null);
        r.setTipo(e.getTipo() != null ? e.getTipo().name() : null);
        r.setPaisId(e.getPais() != null ? e.getPais().getId() : null);
        r.setProvinciaId(e.getProvincia() != null ? e.getProvincia().getId() : null);
        r.setDepartamentoId(e.getDepartamento() != null ? e.getDepartamento().getId() : null);
        r.setLocalidadId(e.getLocalidad() != null ? e.getLocalidad().getId() : null);
        r.setMunicipioId(e.getMunicipio() != null ? e.getMunicipio().getId() : null);
        r.setCalle(e.getCalle());
        r.setNumero(e.getNumero());
        r.setPiso(e.getPiso());
        r.setDepartamentoInterno(e.getDepartamentoInterno());
        r.setCodigoPostal(e.getCodigoPostal());
        r.setReferencia(e.getReferencia());
        r.setActiva(e.getActiva());
        r.setEsPrincipal(e.getEsPrincipal());
        return r;
    }

    private DireccionResponseDTO toResponseDto(DireccionEmpresa e) {
        DireccionResponseDTO r = new DireccionResponseDTO();
        r.setId(e.getId());
        r.setOwnerType("EMPRESA");
        r.setOwnerId(e.getPerfilEmpresa() != null ? e.getPerfilEmpresa().getIdPerfilEmpresa() : null);
        r.setTipo(e.getTipo() != null ? e.getTipo().name() : null);
        r.setPaisId(e.getPais() != null ? e.getPais().getId() : null);
        r.setProvinciaId(e.getProvincia() != null ? e.getProvincia().getId() : null);
        r.setDepartamentoId(e.getDepartamento() != null ? e.getDepartamento().getId() : null);
        r.setLocalidadId(e.getLocalidad() != null ? e.getLocalidad().getId() : null);
        r.setMunicipioId(e.getMunicipio() != null ? e.getMunicipio().getId() : null);
        r.setCalle(e.getCalle());
        r.setNumero(e.getNumero());
        r.setPiso(e.getPiso());
        r.setDepartamentoInterno(e.getDepartamentoInterno());
        r.setCodigoPostal(e.getCodigoPostal());
        r.setReferencia(e.getReferencia());
        r.setActiva(e.getActiva());
        r.setEsPrincipal(e.getEsPrincipal());
        return r;
    }

    private void applyUpdatesToUsuario(DireccionUsuario e, ActualizarDireccionDTO dto) {
        if (dto.getTipo() != null) e.setTipo(DireccionUsuario.TipoDireccion.valueOf(dto.getTipo().toUpperCase()));
        if (dto.getPaisId() != null) { Pais p = new Pais(); p.setId(dto.getPaisId()); e.setPais(p); }
        if (dto.getProvinciaId() != null) { Provincia p = new Provincia(); p.setId(dto.getProvinciaId()); e.setProvincia(p); }
        if (dto.getDepartamentoId() != null) { Departamento d = new Departamento(); d.setId(dto.getDepartamentoId()); e.setDepartamento(d); }
        if (dto.getLocalidadId() != null) { Localidad l = new Localidad(); l.setId(dto.getLocalidadId()); e.setLocalidad(l); }
        if (dto.getMunicipioId() != null) { Municipio m = new Municipio(); m.setId(dto.getMunicipioId()); e.setMunicipio(m); }

        if (dto.getCalle() != null) e.setCalle(dto.getCalle());
        if (dto.getNumero() != null) e.setNumero(dto.getNumero());
        if (dto.getPiso() != null) e.setPiso(dto.getPiso());
        if (dto.getDepartamentoInterno() != null) e.setDepartamentoInterno(dto.getDepartamentoInterno());
        if (dto.getCodigoPostal() != null) e.setCodigoPostal(dto.getCodigoPostal());
        if (dto.getReferencia() != null) e.setReferencia(dto.getReferencia());
        if (dto.getActiva() != null) e.setActiva(dto.getActiva());
        if (dto.getEsPrincipal() != null) e.setEsPrincipal(dto.getEsPrincipal());
    }

    private void applyUpdatesToEmpresa(DireccionEmpresa e, ActualizarDireccionDTO dto) {
        if (dto.getTipo() != null) e.setTipo(DireccionEmpresa.TipoDireccion.valueOf(dto.getTipo().toUpperCase()));
        if (dto.getPaisId() != null) { Pais p = new Pais(); p.setId(dto.getPaisId()); e.setPais(p); }
        if (dto.getProvinciaId() != null) { Provincia p = new Provincia(); p.setId(dto.getProvinciaId()); e.setProvincia(p); }
        if (dto.getDepartamentoId() != null) { Departamento d = new Departamento(); d.setId(dto.getDepartamentoId()); e.setDepartamento(d); }
        if (dto.getLocalidadId() != null) { Localidad l = new Localidad(); l.setId(dto.getLocalidadId()); e.setLocalidad(l); }
        if (dto.getMunicipioId() != null) { Municipio m = new Municipio(); m.setId(dto.getMunicipioId()); e.setMunicipio(m); }

        if (dto.getCalle() != null) e.setCalle(dto.getCalle());
        if (dto.getNumero() != null) e.setNumero(dto.getNumero());
        if (dto.getPiso() != null) e.setPiso(dto.getPiso());
        if (dto.getDepartamentoInterno() != null) e.setDepartamentoInterno(dto.getDepartamentoInterno());
        if (dto.getCodigoPostal() != null) e.setCodigoPostal(dto.getCodigoPostal());
        if (dto.getReferencia() != null) e.setReferencia(dto.getReferencia());
        if (dto.getActiva() != null) e.setActiva(dto.getActiva());
        if (dto.getEsPrincipal() != null) e.setEsPrincipal(dto.getEsPrincipal());
    }
}
