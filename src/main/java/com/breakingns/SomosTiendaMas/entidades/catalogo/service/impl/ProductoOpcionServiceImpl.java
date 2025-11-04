package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ProductoOpcionMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoOpcionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoOpcionServiceImpl implements IProductoOpcionService {
    private final ProductoOpcionRepository productoOpcionRepo;
    private final ProductoValorRepository productoValorRepo;
    private final ProductoRepository productoRepo;
    private final OpcionRepository opcionRepo;
    private final OpcionValorRepository opcionValorRepo;

    public ProductoOpcionServiceImpl(ProductoOpcionRepository productoOpcionRepo,
                                     ProductoValorRepository productoValorRepo,
                                     ProductoRepository productoRepo,
                                     OpcionRepository opcionRepo,
                                     OpcionValorRepository opcionValorRepo) {
        this.productoOpcionRepo = productoOpcionRepo;
        this.productoValorRepo = productoValorRepo;
        this.productoRepo = productoRepo;
        this.opcionRepo = opcionRepo;
        this.opcionValorRepo = opcionValorRepo;
    }

    @Override
    @Transactional
    public void asignarOpciones(ProductoOpcionesAsignarDTO dto, String usuario) {
        if (dto == null || dto.productoId == null) throw new IllegalArgumentException("productoId required");
        var producto = productoRepo.findById(dto.productoId).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));

        // validar que todas las opciones existan
        Set<Long> opcionIds = new HashSet<>();
        for (var s : dto.opciones) opcionIds.add(s.opcionId);
        var opcionesExistentes = opcionRepo.findAllById(opcionIds);
        if (opcionesExistentes.size() != opcionIds.size()) throw new IllegalArgumentException("Alguna opcion no existe");

        int maxOrden = productoOpcionRepo.findMaxOrdenByProductoId(dto.productoId).orElse(-1);
        List<ProductoOpcion> toSavePo = new ArrayList<>();
        List<ProductoValor> toSavePv = new ArrayList<>();

        for (var sel : dto.opciones) {
            if (productoOpcionRepo.existsByProducto_IdAndOpcion_IdAndDeletedAtIsNull(dto.productoId, sel.opcionId)) {
                continue; // si ya existe, por ahora ignoramos (modificar según necesidad)
            }
            ProductoOpcion po = new ProductoOpcion();
            po.setProducto(producto);
            po.setOpcion(opcionRepo.getReferenceById(sel.opcionId));
            po.setCreatedAt(java.time.LocalDateTime.now());
            po.setCreatedBy(usuario);
            po.setActivo(Boolean.TRUE.equals(sel.activo));
            po.setRequerido(Boolean.TRUE.equals(sel.requerido));
            if (sel.orden != null) po.setOrden(sel.orden);
            else po.setOrden(++maxOrden);
            toSavePo.add(po);

            if (sel.opcionValorIds != null && !sel.opcionValorIds.isEmpty()) {
                var valores = opcionValorRepo.findAllById(sel.opcionValorIds);
                if (valores.size() != sel.opcionValorIds.size()) throw new IllegalArgumentException("Algún valor no existe");
                for (var v : valores) {
                    if (!v.getOpcion().getId().equals(sel.opcionId)) throw new IllegalArgumentException("Valor no pertenece a la opción");
                    ProductoValor pv = new ProductoValor();
                    pv.setProducto(producto);
                    pv.setValor(v);
                    pv.setCreatedAt(LocalDateTime.now());
                    pv.setCreatedBy(usuario);
                    toSavePv.add(pv);
                }
            }
        }

        productoOpcionRepo.saveAll(toSavePo);
        productoValorRepo.saveAll(toSavePv);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoConOpcionesDTO obtenerProductoConOpciones(Long productoId) {
        if (productoId == null) throw new IllegalArgumentException("productoId required");
        var producto = productoRepo.findById(productoId).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        var relaciones = productoOpcionRepo.findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        var opciones = relaciones.stream()
                .map(ProductOpcion -> ProductoOpcionMapper.toResumenFromRelacion(ProductOpcion))
                .collect(Collectors.toList());
        return new ProductoConOpcionesDTO(producto.getId(), opciones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoConOpcionesDTO> obtenerTodosConOpciones() {
        var productos = productoRepo.findAll();
        List<ProductoConOpcionesDTO> salida = new ArrayList<>();
        for (var p : productos) {
            var relaciones = productoOpcionRepo.findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(p.getId());
            var opciones = relaciones.stream()
                    .map(ProductOpcion -> ProductoOpcionMapper.toResumenFromRelacion(ProductOpcion))
                    .collect(Collectors.toList());
            salida.add(new ProductoConOpcionesDTO(p.getId(), opciones));
        }
        return salida;
    }

    @Override
    @Transactional
    public void modificarOpciones(Long productoId, ProductoOpcionesAsignarDTO dto, String usuario) {
        if (productoId == null) throw new IllegalArgumentException("productoId required");
        if (dto == null || dto.opciones == null) throw new IllegalArgumentException("payload inválido");

        var producto = productoRepo.findById(productoId).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));

        // existing relaciones
        var existentes = productoOpcionRepo.findByProducto_IdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        //Set<Long> existentesIds = existentes.stream().map(po -> po.getOpcion().getId()).collect(Collectors.toSet());
        Set<Long> incomingIds = dto.opciones.stream().map(o -> o.opcionId).collect(Collectors.toSet());

        // borrar (soft-delete) las que están pero ya no vienen
        List<ProductoOpcion> aBorrar = existentes.stream()
                .filter(po -> !incomingIds.contains(po.getOpcion().getId()))
                .collect(Collectors.toList());
        for (var po : aBorrar) {
            po.setDeletedAt(LocalDateTime.now());
            po.setUpdatedBy(usuario);
        }
        productoOpcionRepo.saveAll(aBorrar);

        // actualizar las existentes (requerido/activo/orden) si vienen en payload
        Map<Long, ProductoOpcion> mapaExistentes = existentes.stream()
                .collect(Collectors.toMap(po -> po.getOpcion().getId(), po -> po));
        int maxOrden = productoOpcionRepo.findMaxOrdenByProductoId(productoId).orElse(-1);
        List<ProductoOpcion> aActualizar = new ArrayList<>();
        List<ProductoOpcion> aCrear = new ArrayList<>();
        List<ProductoValor> valoresCrear = new ArrayList<>();

        for (var sel : dto.opciones) {
            if (mapaExistentes.containsKey(sel.opcionId)) {
                ProductoOpcion po = mapaExistentes.get(sel.opcionId);
                boolean changed = false;
                if (sel.orden != null && !Objects.equals(po.getOrden(), sel.orden)) { po.setOrden(sel.orden); changed = true; }
                if (sel.activo != null && po.isActivo() != sel.activo) { po.setActivo(sel.activo); changed = true; }
                if (sel.requerido != null && po.isRequerido() != sel.requerido) { po.setRequerido(sel.requerido); changed = true; }
                if (changed) {
                    po.setUpdatedAt(LocalDateTime.now());
                    po.setUpdatedBy(usuario);
                    aActualizar.add(po);
                }
            } else {
                // crear nueva relacion
                ProductoOpcion po = new ProductoOpcion();
                po.setProducto(producto);
                po.setOpcion(opcionRepo.getReferenceById(sel.opcionId));
                po.setCreatedAt(LocalDateTime.now());
                po.setCreatedBy(usuario);
                po.setActivo(Boolean.TRUE.equals(sel.activo));
                po.setRequerido(Boolean.TRUE.equals(sel.requerido));
                if (sel.orden != null) po.setOrden(sel.orden);
                else po.setOrden(++maxOrden);
                aCrear.add(po);

                if (sel.opcionValorIds != null && !sel.opcionValorIds.isEmpty()) {
                    var valores = opcionValorRepo.findAllById(sel.opcionValorIds);
                    if (valores.size() != sel.opcionValorIds.size()) throw new IllegalArgumentException("Algún valor no existe");
                    for (var v : valores) {
                        if (!v.getOpcion().getId().equals(sel.opcionId)) throw new IllegalArgumentException("Valor no pertenece a la opción");
                        ProductoValor pv = new ProductoValor();
                        pv.setProducto(producto);
                        pv.setValor(v);
                        pv.setCreatedAt(LocalDateTime.now());
                        pv.setCreatedBy(usuario);
                        valoresCrear.add(pv);
                    }
                }
            }
        }

        productoOpcionRepo.saveAll(aActualizar);
        productoOpcionRepo.saveAll(aCrear);
        productoValorRepo.saveAll(valoresCrear);
    }
}