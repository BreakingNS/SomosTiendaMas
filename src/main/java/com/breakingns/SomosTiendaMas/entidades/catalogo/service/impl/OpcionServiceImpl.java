package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ProductoOpcionMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoOpcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoOpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IOpcionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpcionServiceImpl implements IOpcionService {

    private final ProductoRepository productoRepo;
    private final OpcionRepository opcionRepo;
    private final OpcionValorRepository valorRepo;
    private final ProductoOpcionRepository productoOpcionRepo;

    public OpcionServiceImpl(ProductoRepository productoRepo,
                                     OpcionRepository opcionRepo,
                                     OpcionValorRepository valorRepo,
                                     ProductoOpcionRepository productoOpcionRepo) {
        this.productoRepo = productoRepo;
        this.opcionRepo = opcionRepo;
        this.valorRepo = valorRepo;
        this.productoOpcionRepo = productoOpcionRepo;
    }

    @Override
    public OpcionResponseDTO crearOpcion(OpcionCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        Opcion entidad = OpcionMapper.fromCrear(dto);
        // crear como plantilla (producto_id NULL). Para asignar a un producto usar asignarOpcionAProducto.
        if (entidad.getOrden() == null) {
            Integer next = opcionRepo.findMaxOrden().orElse(-1) + 1;
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
        List<ProductoOpcion> rels = productoOpcionRepo.findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        return rels.stream().map(ProductoOpcionMapper::toResumenFromRelacion).collect(Collectors.toList());
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

    @Override
    @Transactional(readOnly = true)
    public List<OpcionResumenDTO> listarPlantillas() {
        List<Opcion> list = opcionRepo.findByDeletedAtIsNullOrderByOrdenAsc();
        return list.stream().map(OpcionMapper::toResumen).collect(Collectors.toList());
    }

    @Override
    public OpcionResponseDTO asignarOpcionAProducto(Long productoId, Long opcionId) {
        Producto p = productoRepo.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + productoId));
        Opcion o = opcionRepo.findById(opcionId)
                .orElseThrow(() -> new EntityNotFoundException("Opción no encontrada: " + opcionId));

        // evitar duplicados
        if (productoOpcionRepo.existsByProducto_IdAndOpcion_IdAndDeletedAtIsNull(productoId, opcionId)) {
            throw new IllegalStateException("Opción ya asignada al producto");
        }

        Integer next = productoOpcionRepo.findMaxOrdenByProductoId(productoId).orElse(-1) + 1;
        ProductoOpcion po = new ProductoOpcion();
        po.setProducto(p);
        po.setOpcion(o);
        po.setOrden(next);
        ProductoOpcion saved = productoOpcionRepo.save(po);
        return ProductoOpcionMapper.toResponseFromRelacion(saved);
    }

    @Override
    public void desasignarOpcionDeProducto(Long productoId, Long opcionId) {
        ProductoOpcion rel = productoOpcionRepo.findByProducto_IdAndOpcion_IdAndDeletedAtIsNull(productoId, opcionId)
                .orElseThrow(() -> new EntityNotFoundException("Relación no encontrada"));
        rel.setDeletedAt(LocalDateTime.now());
        productoOpcionRepo.save(rel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpcionConValoresSimpleDTO> listarOpcionesConValores() {
        List<Opcion> opciones = opcionRepo.findByDeletedAtIsNullOrderByOrdenAsc();
        if (opciones == null || opciones.isEmpty()) return List.of();

        List<OpcionConValoresSimpleDTO> salida = new java.util.ArrayList<>(opciones.size());
        for (Opcion o : opciones) {
            // obtener valores ordenados para esta opción
            List<OpcionValor> valores = valorRepo.findByOpcionIdOrderByOrdenAsc(o.getId());
            List<OpcionValorSimpleDTO> valoresDto = valores.stream()
                    .map(v -> OpcionValorSimpleDTO.builder()
                            .id(v.getId())
                            .valor(v.getValor())
                            .orden(v.getOrden())
                            .build())
                    .collect(java.util.stream.Collectors.toList());

            OpcionConValoresSimpleDTO dto = OpcionConValoresSimpleDTO.builder()
                    .id(o.getId())
                    .nombre(o.getNombre())
                    .orden(o.getOrden())
                    .tipo(o.getTipo())
                    .valores(valoresDto)
                    .build();

            salida.add(dto);
        }
        return salida;
    }

}