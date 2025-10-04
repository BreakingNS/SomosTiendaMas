package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioService;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventarioServiceImpl implements IInventarioService {

    private final InventarioVarianteRepository inventarioRepo;
    private final VarianteProductoRepository varianteRepo;
    private final MovimientoInventarioRepository movimientoRepo;

    public InventarioServiceImpl(InventarioVarianteRepository inventarioRepo,
                                 VarianteProductoRepository varianteRepo,
                                 MovimientoInventarioRepository movimientoRepo) {
        this.inventarioRepo = inventarioRepo;
        this.varianteRepo = varianteRepo;
        this.movimientoRepo = movimientoRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public long disponibilidad(Long varianteId) {
        VarianteProducto variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));
        Optional<InventarioVariante> invOpt = inventarioRepo.findByVariante(variante);
        return invOpt.map(InventarioVariante::getAvailable).orElse(0L);
    }

    @Override
    public boolean reservar(Long varianteId, long cantidad, String orderRef) {
        if (cantidad <= 0) return false;
        if (orderRef != null && yaAtendimos(orderRef)) return true;

        VarianteProducto variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));
        InventarioVariante inv = inventarioRepo.findByVariante(variante)
                .orElseGet(() -> {
                    InventarioVariante i = new InventarioVariante();
                    i.setVariante(variante);
                    i.setOnHand(0L);
                    i.setReserved(0L);
                    return inventarioRepo.save(i);
                });

        int intentos = 0;
        while (intentos++ < 5) {
            try {
                long disponible = inv.getAvailable();
                if (disponible < cantidad) return false;

                inv.setReserved(inv.getReserved() + cantidad);
                inventarioRepo.saveAndFlush(inv);

                registrarMovimiento(variante, TipoMovimientoInventario.RESERVA, cantidad, orderRef, null, null);
                return true;
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                // recargar y reintentar
                inv = inventarioRepo.findById(inv.getId()).orElseThrow();
            }
        }
        return false;
    }

    @Override
    public boolean liberar(String orderRef) {
        if (orderRef == null || orderRef.isBlank()) return false;
        List<MovimientoInventario> movs = movimientoRepo.findByOrderRef(orderRef);
        if (movs.isEmpty()) return false;

        VarianteProducto variante = movs.get(0).getVariante();
        long pendiente = reservadoPendiente(movs);
        if (pendiente <= 0) return false;

        InventarioVariante inv = inventarioRepo.findByVariante(variante)
                .orElseThrow(() -> new IllegalStateException("Inventario inexistente para variante"));

        int intentos = 0;
        while (intentos++ < 5) {
            try {
                long nuevoReserved = Math.max(0, inv.getReserved() - pendiente);
                inv.setReserved(nuevoReserved);
                inventarioRepo.saveAndFlush(inv);

                registrarMovimiento(variante, TipoMovimientoInventario.LIBERACION, pendiente, orderRef, null, null);
                return true;
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                inv = inventarioRepo.findById(inv.getId()).orElseThrow();
            }
        }
        return false;
    }

    @Override
    public boolean confirmar(String orderRef) {
        if (orderRef == null || orderRef.isBlank()) return false;
        List<MovimientoInventario> movs = movimientoRepo.findByOrderRef(orderRef);
        if (movs.isEmpty()) return false;

        VarianteProducto variante = movs.get(0).getVariante();
        long pendiente = reservadoPendiente(movs);
        if (pendiente <= 0) return true; // ya confirmado o liberado

        InventarioVariante inv = inventarioRepo.findByVariante(variante)
                .orElseThrow(() -> new IllegalStateException("Inventario inexistente para variante"));

        int intentos = 0;
        while (intentos++ < 5) {
            try {
                if (inv.getReserved() < pendiente) return false; // inconsistencia
                if (inv.getOnHand() < pendiente) return false; // no descontar bajo cero

                inv.setReserved(inv.getReserved() - pendiente);
                inv.setOnHand(inv.getOnHand() - pendiente);
                inventarioRepo.saveAndFlush(inv);

                registrarMovimiento(variante, TipoMovimientoInventario.SALIDA_VENTA, pendiente, orderRef, null, null);
                return true;
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                inv = inventarioRepo.findById(inv.getId()).orElseThrow();
            }
        }
        return false;
    }

    @Override
    public void ajusteEntrada(Long varianteId, long cantidad, String referencia, String metadataJson) {
        if (cantidad <= 0) return;
        VarianteProducto variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));
        InventarioVariante inv = inventarioRepo.findByVariante(variante)
                .orElseGet(() -> {
                    InventarioVariante i = new InventarioVariante();
                    i.setVariante(variante);
                    i.setOnHand(0L);
                    i.setReserved(0L);
                    return i;
                });

        int intentos = 0;
        while (intentos++ < 5) {
            try {
                inv.setOnHand(inv.getOnHand() + cantidad);
                inventarioRepo.saveAndFlush(inv);
                registrarMovimiento(variante, TipoMovimientoInventario.ENTRADA_AJUSTE, cantidad, null, referencia, metadataJson);
                return;
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                if (inv.getId() != null) inv = inventarioRepo.findById(inv.getId()).orElseThrow();
            }
        }
    }

    @Override
    public void ajusteSalida(Long varianteId, long cantidad, String referencia, String metadataJson) {
        if (cantidad <= 0) return;
        VarianteProducto variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));
        InventarioVariante inv = inventarioRepo.findByVariante(variante)
                .orElseThrow(() -> new IllegalStateException("Inventario inexistente para variante"));

        int intentos = 0;
        while (intentos++ < 5) {
            try {
                if (inv.getOnHand() < cantidad) {
                    throw new IllegalStateException("Stock insuficiente para ajuste de salida");
                }
                inv.setOnHand(inv.getOnHand() - cantidad);
                inventarioRepo.saveAndFlush(inv);
                registrarMovimiento(variante, TipoMovimientoInventario.SALIDA_VENTA, cantidad, null, referencia, metadataJson);
                return;
            } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
                inv = inventarioRepo.findById(inv.getId()).orElseThrow();
            }
        }
    }

    private boolean yaAtendimos(String orderRef) {
        return movimientoRepo.findFirstByOrderRefOrderByCreatedAtAsc(orderRef).isPresent();
    }

    private long reservadoPendiente(List<MovimientoInventario> movs) {
        long reservas = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimientoInventario.RESERVA)
                .mapToLong(MovimientoInventario::getCantidad).sum();
        long liberaciones = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimientoInventario.LIBERACION)
                .mapToLong(MovimientoInventario::getCantidad).sum();
        long ventas = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimientoInventario.SALIDA_VENTA)
                .mapToLong(MovimientoInventario::getCantidad).sum();
        return reservas - liberaciones - ventas;
    }

    private void registrarMovimiento(VarianteProducto variante,
                                     TipoMovimientoInventario tipo,
                                     long cantidad,
                                     String orderRef,
                                     String referenciaId,
                                     String metadataJson) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setVariante(variante);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setOrderRef(orderRef);
        mov.setReferenciaId(referenciaId);
        mov.setMetadataJson(metadataJson);
        movimientoRepo.save(mov);
    }
}
