package com.breakingns.SomosTiendaMas.entidades.perfil_empresa.service;

import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.dto.PerfilEmpresaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.perfil_empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;
import com.breakingns.SomosTiendaMas.security.exception.PerfilEmpresaNoEncontradoException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerfilEmpresaService implements IPerfilEmpresaService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IPerfilEmpresaRepository perfilEmpresaRepository;

    // ...existing code...

    @Override
    public List<PerfilEmpresaResponseDTO> listarPerfiles() {
        List<PerfilEmpresa> all = traerTodoPerfilEmpresa();
        return all.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public PerfilEmpresaResponseDTO actualizarPerfilEmpresaParcial(Long id, ActualizarPerfilEmpresaDTO dto) {
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new PerfilEmpresaNoEncontradoException(id));

        // merge parcial (solo si no nulos)
        if (dto.getRazonSocial() != null) perfilEmpresa.setRazonSocial(dto.getRazonSocial());
        if (dto.getCondicionIVA() != null) perfilEmpresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.valueOf(dto.getCondicionIVA()));
        if (dto.getEmailEmpresa() != null) perfilEmpresa.setEmailEmpresa(dto.getEmailEmpresa());
        if (dto.getRequiereFacturacion() != null) perfilEmpresa.setRequiereFacturacion(dto.getRequiereFacturacion());
        if (dto.getCategoriaEmpresa() != null) perfilEmpresa.setCategoriaEmpresa(PerfilEmpresa.CategoriaEmpresa.valueOf(dto.getCategoriaEmpresa()));
        if (dto.getSitioWeb() != null) perfilEmpresa.setSitioWeb(dto.getSitioWeb());
        if (dto.getDescripcionEmpresa() != null) perfilEmpresa.setDescripcionEmpresa(dto.getDescripcionEmpresa());
        if (dto.getLogoUrl() != null) perfilEmpresa.setLogoUrl(dto.getLogoUrl());
        if (dto.getColorCorporativo() != null) perfilEmpresa.setColorCorporativo(dto.getColorCorporativo());
        if (dto.getDescripcionCorta() != null) perfilEmpresa.setDescripcionCorta(dto.getDescripcionCorta());
        if (dto.getHorarioAtencion() != null) perfilEmpresa.setHorarioAtencion(dto.getHorarioAtencion());
        if (dto.getDiasLaborales() != null) perfilEmpresa.setDiasLaborales(dto.getDiasLaborales());
        if (dto.getTiempoProcesamientoPedidos() != null) perfilEmpresa.setTiempoProcesamientoPedidos(dto.getTiempoProcesamientoPedidos());

        perfilEmpresa.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfilEmpresa = perfilEmpresaRepository.save(perfilEmpresa);
        return mapToResponseDTO(perfilEmpresa);
    }

    /* 
    // wrapper para mantener compatibilidad con controller PATCH
    @Override
    public PerfilEmpresaResponseDTO actualizarPerfilEmpresaParcial(Long id, ActualizarPerfilEmpresaDTO dto) {
        // si la implementación existente 'actualizarPerfilEmpresa' ya hace overwrite parcial,
        // simplemente la reutilizamos; si no, ajustarla para que respete campos nulos.
        return actualizarPerfilEmpresa(id, dto);
    }*/

    @Override
    public void eliminarPerfilEmpresa(Long id) {
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new PerfilEmpresaNoEncontradoException(id));
        perfilEmpresaRepository.delete(perfilEmpresa);
    }
    /* 
    @Override
    public void eliminarPerfilEmpresa(Long id) {
        eliminarEmpresa(id);
    }*/

    // helper para mapear entidad -> DTO (reutiliza el mapeo ya existente)
    private PerfilEmpresaResponseDTO mapToResponseDTO(PerfilEmpresa perfilEmpresa) {
        PerfilEmpresaResponseDTO response = new PerfilEmpresaResponseDTO();
        response.setIdUsuario(
            perfilEmpresa.getUsuario() != null ? perfilEmpresa.getUsuario().getIdUsuario() : null
        );
        response.setId(perfilEmpresa.getIdPerfilEmpresa());
        response.setRazonSocial(perfilEmpresa.getRazonSocial());
        response.setCuit(perfilEmpresa.getCuit());
        response.setCondicionIVA(perfilEmpresa.getCondicionIVA() != null ? perfilEmpresa.getCondicionIVA().name() : null);
        response.setEstadoAprobado(perfilEmpresa.getEstadoAprobado() != null ? perfilEmpresa.getEstadoAprobado().name() : null);
        response.setEmailEmpresa(perfilEmpresa.getEmailEmpresa());
        response.setRequiereFacturacion(perfilEmpresa.getRequiereFacturacion());
        response.setCategoriaEmpresa(perfilEmpresa.getCategoriaEmpresa() != null ? perfilEmpresa.getCategoriaEmpresa().name() : null);
        response.setSitioWeb(perfilEmpresa.getSitioWeb());
        response.setDescripcionEmpresa(perfilEmpresa.getDescripcionEmpresa());
        response.setLogoUrl(perfilEmpresa.getLogoUrl());
        response.setColorCorporativo(perfilEmpresa.getColorCorporativo());
        response.setDescripcionCorta(perfilEmpresa.getDescripcionCorta());
        response.setHorarioAtencion(perfilEmpresa.getHorarioAtencion());
        response.setDiasLaborales(perfilEmpresa.getDiasLaborales());
        response.setTiempoProcesamientoPedidos(perfilEmpresa.getTiempoProcesamientoPedidos());
        response.setFechaCreacion(perfilEmpresa.getFechaCreacion() != null ? perfilEmpresa.getFechaCreacion().toString() : null);
        response.setFechaUltimaModificacion(perfilEmpresa.getFechaUltimaModificacion() != null ? perfilEmpresa.getFechaUltimaModificacion().toString() : null);
        return response;
    }

    public PerfilEmpresaResponseDTO registrarPerfilEmpresa(RegistroPerfilEmpresaDTO dto) {
        if (perfilEmpresaRepository.existsByCuit(dto.getCuit())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese CUIT");
        }
        PerfilEmpresa perfilEmpresa = new PerfilEmpresa();
        perfilEmpresa.setUsuario(usuarioRepository.findById(dto.getIdUsuario()).orElseThrow());
        perfilEmpresa.setRazonSocial(dto.getRazonSocial());
        perfilEmpresa.setCuit(dto.getCuit());
        perfilEmpresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.valueOf(dto.getCondicionIVA()));
        perfilEmpresa.setEmailEmpresa(dto.getEmailEmpresa());
        perfilEmpresa.setRequiereFacturacion(dto.getRequiereFacturacion());
        perfilEmpresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.PENDIENTE);
        perfilEmpresa.setFechaCreacion(java.time.LocalDateTime.now());
        perfilEmpresa.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfilEmpresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        perfilEmpresa = perfilEmpresaRepository.save(perfilEmpresa);
        return mapToResponseDTO(perfilEmpresa);
    }

    /* 
    public PerfilEmpresaResponseDTO registrarPerfilEmpresa(RegistroPerfilEmpresaDTO dto) {
        // Verificar que el CUIT no exista
        if (perfilEmpresaRepository.existsByCuit(dto.getCuit())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con ese CUIT");
        }

        System.out.println("\n\nIdUsuario responsable recibido: " + dto.getIdUsuario() + "\n\n");

        // Crear entidad PerfilEmpresa y mapear campos
        PerfilEmpresa perfilEmpresa = new PerfilEmpresa();
        // Aquí deberías buscar el usuario por idUsuario y setearlo

        perfilEmpresa.setUsuario(usuarioRepository.findById(dto.getIdUsuario()).orElseThrow());
        perfilEmpresa.setRazonSocial(dto.getRazonSocial());
        perfilEmpresa.setCuit(dto.getCuit());
        perfilEmpresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.valueOf(dto.getCondicionIVA()));
        perfilEmpresa.setEmailEmpresa(dto.getEmailEmpresa());
        perfilEmpresa.setRequiereFacturacion(dto.getRequiereFacturacion());
        perfilEmpresa.setEstadoAprobado(PerfilEmpresa.EstadoAprobado.PENDIENTE);
        perfilEmpresa.setFechaCreacion(java.time.LocalDateTime.now());
        perfilEmpresa.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        perfilEmpresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Guardar en la base de datos
        perfilEmpresa = perfilEmpresaRepository.save(perfilEmpresa);

        // Mapear a PerfilEmpresaResponseDTO
        PerfilEmpresaResponseDTO response = new PerfilEmpresaResponseDTO();
        response.setId(perfilEmpresa.getIdPerfilEmpresa());
        response.setRazonSocial(perfilEmpresa.getRazonSocial());
        response.setCuit(perfilEmpresa.getCuit());
        response.setCondicionIVA(perfilEmpresa.getCondicionIVA().name());
        response.setEstadoAprobado(perfilEmpresa.getEstadoAprobado().name());
        response.setEmailEmpresa(perfilEmpresa.getEmailEmpresa());
        response.setRequiereFacturacion(perfilEmpresa.getRequiereFacturacion());
        response.setFechaCreacion(perfilEmpresa.getFechaCreacion().toString());
        response.setFechaUltimaModificacion(perfilEmpresa.getFechaUltimaModificacion().toString());
        // Puedes mapear más campos si lo necesitas

        return response;
    }*/

    public PerfilEmpresaResponseDTO actualizarPerfilEmpresa(Long id, ActualizarPerfilEmpresaDTO dto) {
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new PerfilEmpresaNoEncontradoException(id));

        // actualización completa (asume todos obligatorios)
        perfilEmpresa.setRazonSocial(dto.getRazonSocial());
        perfilEmpresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.valueOf(dto.getCondicionIVA()));
        perfilEmpresa.setEmailEmpresa(dto.getEmailEmpresa());
        perfilEmpresa.setRequiereFacturacion(dto.getRequiereFacturacion());
        if (dto.getCategoriaEmpresa() != null)
            perfilEmpresa.setCategoriaEmpresa(PerfilEmpresa.CategoriaEmpresa.valueOf(dto.getCategoriaEmpresa()));
        perfilEmpresa.setSitioWeb(dto.getSitioWeb());
        perfilEmpresa.setDescripcionEmpresa(dto.getDescripcionEmpresa());
        perfilEmpresa.setLogoUrl(dto.getLogoUrl());
        perfilEmpresa.setColorCorporativo(dto.getColorCorporativo());
        perfilEmpresa.setDescripcionCorta(dto.getDescripcionCorta());
        perfilEmpresa.setHorarioAtencion(dto.getHorarioAtencion());
        perfilEmpresa.setDiasLaborales(dto.getDiasLaborales());
        perfilEmpresa.setTiempoProcesamientoPedidos(dto.getTiempoProcesamientoPedidos());
        perfilEmpresa.setFechaUltimaModificacion(java.time.LocalDateTime.now());

        perfilEmpresa = perfilEmpresaRepository.save(perfilEmpresa);
        return mapToResponseDTO(perfilEmpresa);
    }

    /*
    public PerfilEmpresaResponseDTO actualizarPerfilEmpresa(Long id, ActualizarPerfilEmpresaDTO dto) {
        // Buscar el perfil empresa por ID
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el perfil empresa con id: " + id));

        // Actualizar los campos permitidos
        perfilEmpresa.setRazonSocial(dto.getRazonSocial());
        perfilEmpresa.setCondicionIVA(PerfilEmpresa.CondicionIVA.valueOf(dto.getCondicionIVA()));
        perfilEmpresa.setEmailEmpresa(dto.getEmailEmpresa());
        perfilEmpresa.setRequiereFacturacion(dto.getRequiereFacturacion());
        // Campos opcionales
        if (dto.getCategoriaEmpresa() != null)
            perfilEmpresa.setCategoriaEmpresa(PerfilEmpresa.CategoriaEmpresa.valueOf(dto.getCategoriaEmpresa()));
        perfilEmpresa.setSitioWeb(dto.getSitioWeb());
        perfilEmpresa.setDescripcionEmpresa(dto.getDescripcionEmpresa());
        perfilEmpresa.setLogoUrl(dto.getLogoUrl());
        perfilEmpresa.setColorCorporativo(dto.getColorCorporativo());
        perfilEmpresa.setDescripcionCorta(dto.getDescripcionCorta());
        perfilEmpresa.setHorarioAtencion(dto.getHorarioAtencion());
        perfilEmpresa.setDiasLaborales(dto.getDiasLaborales());
        perfilEmpresa.setTiempoProcesamientoPedidos(dto.getTiempoProcesamientoPedidos());

        // Actualizar fecha de última modificación
        perfilEmpresa.setFechaUltimaModificacion(java.time.LocalDateTime.now());

        // Guardar cambios
        perfilEmpresa = perfilEmpresaRepository.save(perfilEmpresa);

        // Mapear a PerfilEmpresaResponseDTO
        PerfilEmpresaResponseDTO response = new PerfilEmpresaResponseDTO();
        response.setId(perfilEmpresa.getIdPerfilEmpresa());
        response.setRazonSocial(perfilEmpresa.getRazonSocial());
        response.setCuit(perfilEmpresa.getCuit());
        response.setCondicionIVA(perfilEmpresa.getCondicionIVA().name());
        response.setEstadoAprobado(perfilEmpresa.getEstadoAprobado().name());
        response.setEmailEmpresa(perfilEmpresa.getEmailEmpresa());
        response.setRequiereFacturacion(perfilEmpresa.getRequiereFacturacion());
        response.setCategoriaEmpresa(perfilEmpresa.getCategoriaEmpresa() != null ? perfilEmpresa.getCategoriaEmpresa().name() : null);
        response.setSitioWeb(perfilEmpresa.getSitioWeb());
        response.setDescripcionEmpresa(perfilEmpresa.getDescripcionEmpresa());
        response.setLogoUrl(perfilEmpresa.getLogoUrl());
        response.setColorCorporativo(perfilEmpresa.getColorCorporativo());
        response.setDescripcionCorta(perfilEmpresa.getDescripcionCorta());
        response.setHorarioAtencion(perfilEmpresa.getHorarioAtencion());
        response.setDiasLaborales(perfilEmpresa.getDiasLaborales());
        response.setTiempoProcesamientoPedidos(perfilEmpresa.getTiempoProcesamientoPedidos());
        response.setFechaCreacion(perfilEmpresa.getFechaCreacion().toString());
        response.setFechaUltimaModificacion(perfilEmpresa.getFechaUltimaModificacion().toString());

        return response;
    }*/

    @Override
    public PerfilEmpresaResponseDTO obtenerPerfilEmpresa(Long id) {
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new PerfilEmpresaNoEncontradoException(id));
        return mapToResponseDTO(perfilEmpresa);
    }

    /*
    public PerfilEmpresaResponseDTO obtenerPerfilEmpresa(Long id) {
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el perfil empresa con id: " + id));



        PerfilEmpresaResponseDTO response = new PerfilEmpresaResponseDTO();
        response.setIdUsuario(
            perfilEmpresa.getUsuario() != null ? perfilEmpresa.getUsuario().getIdUsuario() : null
        );
        response.setId(perfilEmpresa.getIdPerfilEmpresa());
        response.setRazonSocial(perfilEmpresa.getRazonSocial());
        response.setCuit(perfilEmpresa.getCuit());
        response.setCondicionIVA(perfilEmpresa.getCondicionIVA().name());
        response.setEstadoAprobado(perfilEmpresa.getEstadoAprobado().name());
        response.setEmailEmpresa(perfilEmpresa.getEmailEmpresa());
        response.setRequiereFacturacion(perfilEmpresa.getRequiereFacturacion());
        response.setCategoriaEmpresa(perfilEmpresa.getCategoriaEmpresa() != null ? perfilEmpresa.getCategoriaEmpresa().name() : null);
        response.setSitioWeb(perfilEmpresa.getSitioWeb());
        response.setDescripcionEmpresa(perfilEmpresa.getDescripcionEmpresa());
        response.setLogoUrl(perfilEmpresa.getLogoUrl());
        response.setColorCorporativo(perfilEmpresa.getColorCorporativo());
        response.setDescripcionCorta(perfilEmpresa.getDescripcionCorta());
        response.setHorarioAtencion(perfilEmpresa.getHorarioAtencion());
        response.setDiasLaborales(perfilEmpresa.getDiasLaborales());
        response.setTiempoProcesamientoPedidos(perfilEmpresa.getTiempoProcesamientoPedidos());
        response.setFechaCreacion(perfilEmpresa.getFechaCreacion().toString());
        response.setFechaUltimaModificacion(perfilEmpresa.getFechaUltimaModificacion().toString());

        return response;
    }*/

    public List<PerfilEmpresa> traerTodoPerfilEmpresa() {// SOLO TESTEO ANITA
        return perfilEmpresaRepository.findAll();
    }

    public void eliminarEmpresa(Long id) {
        eliminarPerfilEmpresa(id);
    }

    @Override
    public boolean existsByCuit(String cuit) {
        if (cuit == null) return false;
        String normalized = cuit.replaceAll("[^0-9]", "");
        return perfilEmpresaRepository.existsByCuit(normalized);
    }

    /* 
    public void eliminarEmpresa(Long id) {
        perfilEmpresaRepository.deleteById(id);
    }*/

    // Puedes agregar más métodos según la lógica de negocio
}
