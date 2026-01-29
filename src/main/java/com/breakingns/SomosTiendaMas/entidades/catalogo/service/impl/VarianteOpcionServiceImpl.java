package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.OpcionConValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.OpcionValorMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesModificarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VarianteOpcionMapper;
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
    private final OpcionValorRepository opcionValorRepo;
    private final VarianteOpcionRepository varianteOpcionRepo;
    private final OpcionRepository opcionRepo;

    private static final Logger log = LoggerFactory.getLogger(VarianteOpcionServiceImpl.class);


    public VarianteOpcionServiceImpl(VarianteValorRepository varianteValorRepo,
                                     VarianteRepository varianteRepo,
                                     OpcionValorRepository opcionValorRepo,
                                     VarianteOpcionRepository varianteOpcionRepo,
                                     OpcionRepository opcionRepo) {
        this.varianteValorRepo = varianteValorRepo;
        this.varianteRepo = varianteRepo;
        this.opcionValorRepo = opcionValorRepo;
        this.varianteOpcionRepo = varianteOpcionRepo;
        this.opcionRepo = opcionRepo;
    }

    @Override
    @Transactional
    public void asignarOpciones(VarianteOpcionesAsignarDTO dto, String usuario) {
        if (dto == null || dto.varianteId == null) throw new IllegalArgumentException("varianteId required");
        var variante = varianteRepo.findById(dto.varianteId).orElseThrow(() -> new NoSuchElementException("Variante no encontrado"));

        // validar que las opciones solicitadas estén declaradas para el producto padre
        Set<Long> opcionIds = new HashSet<>();
        for (var s : dto.opciones) opcionIds.add(s.opcionId);
        // validar que las opciones solicitadas estén declaradas para el producto padre
        List<Long> missing = new ArrayList<>();
        for (Long opcionId : opcionIds) {
            boolean existe = varianteOpcionRepo.existsByVariante_Producto_IdAndOpcion_IdAndDeletedAtIsNull(
                variante.getProducto().getId(), opcionId);
            if (!existe) missing.add(opcionId);
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Alguna opcion no pertenece al producto: productoId=" + variante.getProducto().getId() + " opcionIds=" + missing);
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
        // Consultar las relaciones específicas para esta variante para evitar duplicados
        var relaciones = varianteOpcionRepo.findByVariante_IdAndDeletedAtIsNullOrderByOrdenAsc(varianteId);
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
                var relaciones = varianteOpcionRepo.findByVariante_Producto_IdAndDeletedAtIsNullOrderByOrdenAsc(p.getProducto().getId());
                var opciones = relaciones.stream()
                    .map(productOpcion -> VarianteOpcionMapper.toResumenFromRelacion(productOpcion))
                    .collect(Collectors.toList());
            salida.add(new VarianteConOpcionesDTO(p.getId(), opciones));
        }
        return salida;
    }

    @Override
    @Transactional
    public void modificarOpciones(Long varianteId, VarianteOpcionesModificarDTO dto, String usuario) {
        if (varianteId == null) throw new IllegalArgumentException("varianteId required");
        if (dto == null || dto.opciones == null) throw new IllegalArgumentException("payload inválido");

        var variante = varianteRepo.findById(varianteId).orElseThrow(() -> new NoSuchElementException("Variante no encontrado"));

        // precargar existentes a nivel variante
        List<VarianteValor> existentesPv = varianteValorRepo.findByVariante_IdAndDeletedAtIsNull(varianteId);
        Map<Long, List<VarianteValor>> existentesPorOpcion = new HashMap<>();
        for (var pv : existentesPv) {
            var opcion = pv.getValor() != null && pv.getValor().getOpcion() != null ? pv.getValor().getOpcion().getId() : null;
            if (opcion != null) existentesPorOpcion.computeIfAbsent(opcion, k -> new ArrayList<>()).add(pv);
        }

        List<VarianteValor> aCrear = new ArrayList<>();
        List<VarianteValor> aBorrar = new ArrayList<>();

        for (var sel : dto.opciones) {
            if (sel.opcionId == null) throw new IllegalArgumentException("opcionId requerido");

            // validar que la opcion pertenece al producto padre
            boolean pertenece = varianteOpcionRepo.existsByVariante_Producto_IdAndOpcion_IdAndDeletedAtIsNull(variante.getProducto().getId(), sel.opcionId);
            if (!pertenece) throw new IllegalArgumentException("Alguna opcion no pertenece al producto: productoId=" + variante.getProducto().getId() + " opcionId=" + sel.opcionId);

            String opAction = sel.action != null ? sel.action.toLowerCase() : "update";

            if ("delete".equals(opAction)) {
                // eliminar todos los variante_valor para esta opcion
                var existList = existentesPorOpcion.getOrDefault(sel.opcionId, List.of());
                for (var pv : existList) {
                    pv.setDeletedAt(LocalDateTime.now());
                    pv.setUpdatedBy(usuario);
                    aBorrar.add(pv);
                }
                continue;
            }

            // procesar valores si vienen
            if (sel.valores != null && !sel.valores.isEmpty()) {
                var existList = existentesPorOpcion.getOrDefault(sel.opcionId, List.of());

                // obtener metadata de la opción para decidir si es multiselect
                var opcionEntity = opcionRepo.findById(sel.opcionId).orElseThrow(() -> new NoSuchElementException("Opcion no encontrada=" + sel.opcionId));
                String tipo = opcionEntity.getTipo();

                boolean isMulti = tipo != null && "multiselect".equalsIgnoreCase(tipo);

                // Si no es multiselect, al recibir un add/update de valor, eliminamos los existentes
                // para asegurar que quede un único valor vigente.
                boolean anyAddOrUpdate = sel.valores.stream().anyMatch(v -> v.action == null || !"delete".equalsIgnoreCase(v.action));
                if (!isMulti && anyAddOrUpdate && !existList.isEmpty()) {
                    for (var pv : existList) {
                        pv.setDeletedAt(LocalDateTime.now());
                        pv.setUpdatedBy(usuario);
                        aBorrar.add(pv);
                    }
                }

                for (var vdto : sel.valores) {
                    String valAction = vdto.action != null ? vdto.action.toLowerCase() : "add";
                    if ("add".equals(valAction)) {
                        if (vdto.id == null) throw new IllegalArgumentException("valor.id requerido para add en variant-level");
                        var opcionValor = opcionValorRepo.findById(vdto.id).orElseThrow(() -> new NoSuchElementException("OpcionValor no encontrado=" + vdto.id));
                        if (!opcionValor.getOpcion().getId().equals(sel.opcionId)) throw new IllegalArgumentException("Valor no pertenece a la opción");
                        boolean already = existList.stream().anyMatch(pv -> pv.getValor() != null && pv.getValor().getId().equals(opcionValor.getId()) && pv.getDeletedAt() == null);
                        if (!already) {
                            VarianteValor pv = new VarianteValor();
                            pv.setVariante(variante);
                            pv.setValor(opcionValor);
                            pv.setCreatedAt(LocalDateTime.now());
                            pv.setCreatedBy(usuario);
                            aCrear.add(pv);
                        }
                    } else if ("delete".equals(valAction)) {
                        if (vdto.id == null) throw new IllegalArgumentException("valor.id requerido para delete");
                        for (var pv : existList) {
                            if (pv.getValor() != null && pv.getValor().getId().equals(vdto.id) && pv.getDeletedAt() == null) {
                                pv.setDeletedAt(LocalDateTime.now());
                                pv.setUpdatedBy(usuario);
                                aBorrar.add(pv);
                            }
                        }
                    } else {
                        // update no aplica mucho a VarianteValor; ignorar o extender según necesidad
                    }
                }
            } else {
                // si no vienen valores y action es update -> nada; si se quería borrar, habríamos tenido action=delete
            }
        }

        if (!aBorrar.isEmpty()) varianteValorRepo.saveAll(aBorrar);
        if (!aCrear.isEmpty()) varianteValorRepo.saveAll(aCrear);
    }

    @Override
    @Transactional
    public void modificarOpcionesPorProducto(Long productoId, VarianteOpcionesModificarDTO dto, String usuario) {
        if (productoId == null) throw new IllegalArgumentException("productoId required");
        if (dto == null || dto.opciones == null) throw new IllegalArgumentException("payload inválido");

        // localizar variante default para el producto donde persistir las relaciones por defecto
        var defaultVariante = varianteRepo.findDefaultByProductoId(productoId).orElseThrow(() -> new NoSuchElementException("Variante default no encontrada para producto=" + productoId));

        // existing relaciones para el producto
        List<VarianteOpcion> existentes = varianteOpcionRepo.findByVariante_Producto_IdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        Map<Long, VarianteOpcion> existentesMap = existentes.stream()
            .filter(vo -> vo.getOpcion() != null)
            .collect(Collectors.toMap(vo -> vo.getOpcion().getId(), vo -> vo, (a, b) -> a));

        Set<Long> incomingIds = dto.opciones.stream().map(o -> o.opcionId).collect(Collectors.toSet());

        List<VarianteOpcion> toSave = new ArrayList<>();
        List<VarianteOpcion> toDelete = new ArrayList<>();

        // marcar para borrado (soft) las opciones existentes que ya no vienen
        for (var entry : existentes) {
            Long opcionId = entry.getOpcion() != null ? entry.getOpcion().getId() : null;
            if (opcionId != null && !incomingIds.contains(opcionId)) {
                entry.setDeletedAt(LocalDateTime.now());
                entry.setUpdatedBy(usuario);
                toDelete.add(entry);
            }
        }

        int maxOrden = varianteOpcionRepo.findMaxOrdenByProductoId(productoId).orElse(0);

        for (var sel : dto.opciones) {
            if (sel.opcionId == null) throw new IllegalArgumentException("opcionId requerido");

            VarianteOpcion existente = existentesMap.get(sel.opcionId);
            if (existente != null) {
                if (sel.orden != null) existente.setOrden(sel.orden);
                if (sel.requerido != null) existente.setRequerido(sel.requerido);
                if (sel.activo != null) existente.setActivo(sel.activo);
                existente.setUpdatedBy(usuario);
                existente.setUpdatedAt(LocalDateTime.now());
                toSave.add(existente);
            } else {
                var opcion = opcionRepo.findByIdAndDeletedAtIsNull(sel.opcionId).orElseThrow(() -> new NoSuchElementException("Opcion no encontrada=" + sel.opcionId));
                VarianteOpcion vo = new VarianteOpcion();
                vo.setVariante(defaultVariante);
                vo.setOpcion(opcion);
                vo.setOrden(sel.orden != null ? sel.orden : ++maxOrden);
                vo.setRequerido(sel.requerido != null ? sel.requerido : false);
                vo.setActivo(sel.activo != null ? sel.activo : true);
                vo.setCreatedAt(LocalDateTime.now());
                vo.setCreatedBy(usuario);
                toSave.add(vo);
            }
        }

        if (!toDelete.isEmpty()) varianteOpcionRepo.saveAll(toDelete);
        if (!toSave.isEmpty()) varianteOpcionRepo.saveAll(toSave);
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