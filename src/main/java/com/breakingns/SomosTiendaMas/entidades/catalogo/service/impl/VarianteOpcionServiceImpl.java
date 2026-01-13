package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.OpcionConValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VarianteOpcionMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoOpcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteOpcionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class VarianteOpcionServiceImpl implements IVarianteOpcionService {
    private final VarianteValorRepository varianteValorRepo;
    private final VarianteRepository varianteRepo;
    private final OpcionRepository opcionRepo;
    private final OpcionValorRepository opcionValorRepo;
    private final ProductoOpcionRepository productoOpcionRepo;

    private static final Logger log = LoggerFactory.getLogger(VarianteOpcionServiceImpl.class);


    public VarianteOpcionServiceImpl(ProductoOpcionRepository productoOpcionRepo,
                                     VarianteValorRepository varianteValorRepo,
                                     VarianteRepository varianteRepo,
                                     OpcionRepository opcionRepo,
                                     OpcionValorRepository opcionValorRepo) {
        this.productoOpcionRepo = productoOpcionRepo;
        this.varianteValorRepo = varianteValorRepo;
        this.varianteRepo = varianteRepo;
        this.opcionRepo = opcionRepo;
        this.opcionValorRepo = opcionValorRepo;
    }

    @Override
    @Transactional
    public void asignarOpciones(VarianteOpcionesAsignarDTO dto, String usuario) {
        if (dto == null || dto.varianteId == null) throw new IllegalArgumentException("varianteId required");
        var variante = varianteRepo.findById(dto.varianteId).orElseThrow(() -> new NoSuchElementException("Variante no encontrado"));

        // validar que las opciones solicitadas estén declaradas para el producto padre
        Set<Long> opcionIds = new HashSet<>();
        for (var s : dto.opciones) opcionIds.add(s.opcionId);
        for (Long opcionId : opcionIds) {
            boolean existe = productoOpcionRepo.existsByProducto_IdAndOpcion_IdAndDeletedAtIsNull(
                    variante.getProducto().getId(), opcionId);
            if (!existe) throw new IllegalArgumentException("Alguna opcion no pertenece al producto");
        }

        List<VarianteValor> toSavePv = new ArrayList<>();
        for (var sel : dto.opciones) {
            if (sel.opcionValorIds != null && !sel.opcionValorIds.isEmpty()) {
                var valores = opcionValorRepo.findAllById(sel.opcionValorIds);
                if (valores.size() != sel.opcionValorIds.size()) throw new IllegalArgumentException("Algún valor no existe");
                for (var v : valores) {
                    if (!v.getOpcion().getId().equals(sel.opcionId)) throw new IllegalArgumentException("Valor no pertenece a la opción");
                    VarianteValor pv = new VarianteValor();
                    pv.setVariante(variante);
                    pv.setValor(v);
                    pv.setCreatedAt(LocalDateTime.now());
                    pv.setCreatedBy(usuario);
                    toSavePv.add(pv);
                }
            }
        }

        varianteValorRepo.saveAll(toSavePv);
    }

    @Override
    @Transactional(readOnly = true)
    public VarianteConOpcionesDTO obtenerVarianteConOpciones(Long varianteId) {
        if (varianteId == null) throw new IllegalArgumentException("varianteId required");
        var variante = varianteRepo.findById(varianteId).orElseThrow(() -> new NoSuchElementException("Variante no encontrado"));
        var relaciones = productoOpcionRepo.findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(variante.getProducto().getId());
        var opciones = relaciones.stream()
            .map(productOpcion -> VarianteOpcionMapper.toResumenFromRelacion(productOpcion))
            .collect(Collectors.toList());
        return new VarianteConOpcionesDTO(variante.getId(), opciones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteConOpcionesDTO> obtenerTodosConOpciones() {
        var variantes = varianteRepo.findAll();
        List<VarianteConOpcionesDTO> salida = new ArrayList<>();
        for (var p : variantes) {
                var relaciones = productoOpcionRepo.findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(p.getProducto().getId());
                var opciones = relaciones.stream()
                    .map(productOpcion -> VarianteOpcionMapper.toResumenFromRelacion(productOpcion))
                    .collect(Collectors.toList());
            salida.add(new VarianteConOpcionesDTO(p.getId(), opciones));
        }
        return salida;
    }

    @Override
    @Transactional
    public void modificarOpciones(Long varianteId, VarianteOpcionesAsignarDTO dto, String usuario) {
        if (varianteId == null) throw new IllegalArgumentException("varianteId required");
        if (dto == null || dto.opciones == null) throw new IllegalArgumentException("payload inválido");

        var variante = varianteRepo.findById(varianteId).orElseThrow(() -> new NoSuchElementException("Variante no encontrado"));

        // existing relaciones
        // En el nuevo modelo no gestionamos relaciones VarianteOpcion; gestionamos solo VarianteValor (overrides)
        List<VarianteValor> existentesPv = varianteValorRepo.findByVariante_IdAndDeletedAtIsNull(varianteId);
        Map<Long, List<VarianteValor>> existentesPorOpcion = new HashMap<>();
        for (var pv : existentesPv) {
            var opcion = pv.getValor() != null && pv.getValor().getOpcion() != null ? pv.getValor().getOpcion().getId() : null;
            if (opcion != null) existentesPorOpcion.computeIfAbsent(opcion, k -> new ArrayList<>()).add(pv);
        }

        List<VarianteValor> aCrear = new ArrayList<>();
        List<VarianteValor> aBorrar = new ArrayList<>();

        Set<Long> incomingIds = dto.opciones.stream().map(o -> o.opcionId).collect(Collectors.toSet());

        // borrar variant-level values de opciones que ya no vienen en payload
        for (var entry : existentesPorOpcion.entrySet()) {
            if (!incomingIds.contains(entry.getKey())) {
                for (var pv : entry.getValue()) {
                    pv.setDeletedAt(LocalDateTime.now());
                    pv.setUpdatedBy(usuario);
                    aBorrar.add(pv);
                }
            }
        }

        // procesar incoming: validar existencia en producto y crear/ajustar valores
        for (var sel : dto.opciones) {
            boolean pertenece = productoOpcionRepo.existsByProducto_IdAndOpcion_IdAndDeletedAtIsNull(variante.getProducto().getId(), sel.opcionId);
            if (!pertenece) throw new IllegalArgumentException("Alguna opcion no pertenece al producto");

            if (sel.opcionValorIds != null && !sel.opcionValorIds.isEmpty()) {
                var valores = opcionValorRepo.findAllById(sel.opcionValorIds);
                if (valores.size() != sel.opcionValorIds.size()) throw new IllegalArgumentException("Algún valor no existe");

                // soft-delete existentes que no aparecen
                var existList = existentesPorOpcion.getOrDefault(sel.opcionId, List.of());
                Set<Long> incomingValIds = new HashSet<>(sel.opcionValorIds);
                for (var pv : existList) {
                    if (!incomingValIds.contains(pv.getValor().getId())) {
                        pv.setDeletedAt(LocalDateTime.now());
                        pv.setUpdatedBy(usuario);
                        aBorrar.add(pv);
                    }
                }

                for (var v : valores) {
                    if (!v.getOpcion().getId().equals(sel.opcionId)) throw new IllegalArgumentException("Valor no pertenece a la opción");
                    boolean already = existList.stream().anyMatch(pv -> pv.getValor() != null && pv.getValor().getId().equals(v.getId()) && pv.getDeletedAt() == null);
                    if (!already) {
                        VarianteValor pv = new VarianteValor();
                        pv.setVariante(variante);
                        pv.setValor(v);
                        pv.setCreatedAt(LocalDateTime.now());
                        pv.setCreatedBy(usuario);
                        aCrear.add(pv);
                    }
                }
            } else {
                // si no vienen valores para esta opcion, borrar los existentes
                var existList = existentesPorOpcion.getOrDefault(sel.opcionId, List.of());
                for (var pv : existList) {
                    pv.setDeletedAt(LocalDateTime.now());
                    pv.setUpdatedBy(usuario);
                    aBorrar.add(pv);
                }
            }
        }

        if (!aBorrar.isEmpty()) varianteValorRepo.saveAll(aBorrar);
        if (!aCrear.isEmpty()) varianteValorRepo.saveAll(aCrear);
    }

    @Override
    @Transactional(readOnly = true)
    public VarianteConOpcionesValoresDTO obtenerVarianteConOpcionesConValores(Long varianteId) {
        if (varianteId == null) throw new IllegalArgumentException("varianteId required");
        var variante = varianteRepo.findById(varianteId).orElseThrow(() -> new NoSuchElementException("Variante no encontrado"));
        // precargar todos los valores del variante para evitar N+1
        List<VarianteValor> pvAll = varianteValorRepo.findByVariante_IdAndDeletedAtIsNull(varianteId);

        log.debug("obtenerVarianteConOpcionesConValores -> varianteId={}, pvAllCount={}", varianteId, pvAll.size());

        // Agrupar por opcion (derivada desde OpcionValor -> Opcion)
        Map<Long, List<VarianteValor>> porOpcion = pvAll.stream()
            .filter(pv -> pv.getValor() != null && pv.getValor().getOpcion() != null && pv.getDeletedAt() == null)
            .collect(Collectors.groupingBy(pv -> pv.getValor().getOpcion().getId()));

        List<OpcionConValoresDTO> opciones = new ArrayList<>();
        for (Map.Entry<Long, List<VarianteValor>> e : porOpcion.entrySet()) {
            Long opcionId = e.getKey();
            List<VarianteValor> lista = e.getValue();

            List<OpcionValorResponseDTO> valoresDto = lista.stream()
                .map(pv -> OpcionValorMapper.toResponse(pv.getValor()))
                .collect(Collectors.toList());

            // obtener metadata de la opcion desde el primer valor
            var primera = lista.get(0).getValor().getOpcion();
            String nombre = primera != null ? primera.getNombre() : null;
            Integer orden = primera != null ? primera.getOrden() : null;

            opciones.add(new OpcionConValoresDTO(opcionId, nombre, orden, valoresDto));
        }

        return new VarianteConOpcionesValoresDTO(variante.getId(), opciones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VarianteConOpcionesValoresDTO> obtenerTodosConOpcionesConValores() {
        var variantes = varianteRepo.findAll();
        List<VarianteConOpcionesValoresDTO> salida = new ArrayList<>();
        for (var p : variantes) {
            salida.add(obtenerVarianteConOpcionesConValores(p.getId()));
        }
        return salida;
    }
}