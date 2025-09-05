package com.breakingns.SomosTiendaMas.entidades.empresa.service;

import com.breakingns.SomosTiendaMas.entidades.empresa.dto.RegistroPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.ActualizarPerfilEmpresaDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.dto.PerfilEmpresaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.empresa.model.PerfilEmpresa;
import com.breakingns.SomosTiendaMas.entidades.empresa.repository.IPerfilEmpresaRepository;
import com.breakingns.SomosTiendaMas.entidades.usuario.repository.IUsuarioRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import java.util.Optional;

@Service
public class PerfilEmpresaService implements IPerfilEmpresaService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IPerfilEmpresaRepository perfilEmpresaRepository;

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

        // Guardar en la base de datos
        perfilEmpresa = perfilEmpresaRepository.save(perfilEmpresa);

        // Mapear a PerfilEmpresaResponseDTO
        PerfilEmpresaResponseDTO response = new PerfilEmpresaResponseDTO();
        response.setId(perfilEmpresa.getId());
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
    }

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
        response.setId(perfilEmpresa.getId());
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
    }

    public PerfilEmpresaResponseDTO obtenerPerfilEmpresa(Long id) {
        PerfilEmpresa perfilEmpresa = perfilEmpresaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró el perfil empresa con id: " + id));



        PerfilEmpresaResponseDTO response = new PerfilEmpresaResponseDTO();
        response.setIdUsuario(
            perfilEmpresa.getUsuario() != null ? perfilEmpresa.getUsuario().getIdUsuario() : null
        );
        response.setId(perfilEmpresa.getId());
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
    }

    public List<PerfilEmpresa> traerTodoPerfilEmpresa() {// SOLO TESTEO ANITA
        return perfilEmpresaRepository.findAll();
    }

    public void eliminarEmpresa(Long id) {
        perfilEmpresaRepository.deleteById(id);
    }

    // Puedes agregar más métodos según la lógica de negocio
}
