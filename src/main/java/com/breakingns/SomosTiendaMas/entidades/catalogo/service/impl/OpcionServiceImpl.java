package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpcionServiceImpl implements IOpcionService {

    private final ProductoRepository productoRepo;
    private final OpcionRepository opcionRepo;
    private final OpcionValorRepository valorRepo;

    public OpcionServiceImpl(ProductoRepository productoRepo,
                                     OpcionRepository opcionRepo,
                                     OpcionValorRepository valorRepo) {
        this.productoRepo = productoRepo;
        this.opcionRepo = opcionRepo;
        this.valorRepo = valorRepo;
    }

    @Override
    public OpcionResponseDTO crearOpcion(OpcionCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        Opcion entidad = OpcionMapper.fromCrear(dto);
        if (dto.getProductoId() != null) {
            Producto p = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));
            entidad.setProducto(p);
        }
        // calcular orden si no viene
        if (entidad.getOrden() == null) {
            List<Opcion> actuales = opcionRepo.findByProductoIdOrderByOrdenAsc(dto.getProductoId());
            int next = actuales.isEmpty() ? 0 : (actuales.get(actuales.size()-1).getOrden() + 1);
            entidad.setOrden(next);
        }
        Opcion saved = opcionRepo.save(entidad);
        return OpcionMapper.toResponse(saved);
    }

    @Override
    public OpcionResponseDTO actualizarOpcion(Long opcionId, OpcionActualizarDTO dto) {
        Opcion e = opcionRepo.findById(opcionId)
                .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + opcionId));
        OpcionMapper.applyActualizar(dto, e);
        Opcion updated = opcionRepo.save(e);
        return OpcionMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionResponseDTO obtenerOpcionPorId(Long opcionId) {
        Opcion e = opcionRepo.findById(opcionId)
                .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + opcionId));
        return OpcionMapper.toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionResumenDTO> listarOpcionesPorProductoId(Long productoId) {
        List<Opcion> list = opcionRepo.findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        return list.stream().map(OpcionMapper::toResumen).collect(Collectors.toList());
    }

    @Override
    public void eliminarOpcion(Long opcionId) {
        Opcion e = opcionRepo.findById(opcionId)
                .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + opcionId));
        e.setDeletedAt(LocalDateTime.now());
        opcionRepo.save(e);
    }

    @Override
    public OpcionValorResponseDTO crearValor(OpcionValorCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        OpcionValor entidad = OpcionValorMapper.fromCrear(dto);
        if (dto.getOpcionId() != null) {
            Opcion o = opcionRepo.findById(dto.getOpcionId())
                    .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + dto.getOpcionId()));
            entidad.setOpcion(o);
        } else {
            throw new IllegalArgumentException("opcionId es requerido");
        }
        // calcular orden si no viene
        if (entidad.getOrden() == null) {
            List<OpcionValor> actuales = valorRepo.findByOpcionIdOrderByOrdenAsc(dto.getOpcionId());
            int next = actuales.isEmpty() ? 0 : (actuales.get(actuales.size()-1).getOrden() + 1);
            entidad.setOrden(next);
        }
        OpcionValor saved = valorRepo.save(entidad);
        return OpcionValorMapper.toResponse(saved);
    }

    @Override
    public OpcionValorResponseDTO actualizarValor(Long valorId, OpcionValorActualizarDTO dto) {
        OpcionValor v = valorRepo.findById(valorId)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + valorId));
        OpcionValorMapper.applyActualizar(dto, v);
        OpcionValor updated = valorRepo.save(v);
        return OpcionValorMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionValorResponseDTO obtenerValorPorId(Long valorId) {
        OpcionValor v = valorRepo.findById(valorId)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + valorId));
        return OpcionValorMapper.toResponse(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionValorResponseDTO> listarValoresPorOpcionId(Long opcionId) {
        Opcion o = opcionRepo.findById(opcionId)
                .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + opcionId));
        List<OpcionValor> list = valorRepo.findByOpcionIdOrderByOrdenAsc(opcionId);
        return list.stream().map(OpcionValorMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void eliminarValor(Long valorId) {
        OpcionValor v = valorRepo.findById(valorId)
                .orElseThrow(() -> new EntityNotFoundException("Valor no encontrado: " + valorId));
        v.setDeletedAt(LocalDateTime.now());
        valorRepo.save(v);
    }
}