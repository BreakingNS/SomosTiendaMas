package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VarianteInventarioMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMovimientoInventarioService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;

@Service
@Transactional
public class InventarioVarianteService implements IInventarioVarianteService {

    private final InventarioVarianteRepository repo;
    private final VarianteRepository varianteRepo;
    private final IMovimientoInventarioService movimientoService;
    private final MovimientoInventarioRepository movimientoRepo;

    public InventarioVarianteService(InventarioVarianteRepository repo, VarianteRepository varianteRepo,
                                     IMovimientoInventarioService movimientoService,
                                     MovimientoInventarioRepository movimientoRepo) {
        this.repo = repo;
        this.varianteRepo = varianteRepo;
        this.movimientoService = movimientoService;
        this.movimientoRepo = movimientoRepo;
    }

    @Override
    public InventarioVarianteDTO crear(InventarioVarianteDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto null");
        if (dto.getVarianteId() == null) throw new IllegalArgumentException("varianteId es requerido");

        // asociar inventario a la variante default del Variante
        Variante variante = varianteRepo.findById(dto.getVarianteId())
            .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + dto.getVarianteId()));

        InventarioVariante entidad = VarianteInventarioMapper.fromDto(dto);
        entidad.setVariante(variante);
        InventarioVariante saved = repo.save(entidad);
        return VarianteInventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioVarianteDTO obtenerPorVarianteId(Long varianteId) {
        if (varianteId == null) throw new IllegalArgumentException("varianteId es requerido");
        InventarioVariante inv = repo.findByVarianteId(varianteId)
            .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para variante: " + varianteId));
        return VarianteInventarioMapper.toDto(inv);
    }

    // helper para normalizar Integer/Long/primitive a long (seguro ante null)
    private static long safeNumber(Number n) {
        return n == null ? 0L : n.longValue();
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO disponibilidad(Long varianteId) {
        InventarioVariante inv = repo.findByVarianteId(varianteId).orElse(null);
        long onHand = safeNumber(inv != null ? inv.getOnHand() : null);
        long reserved = safeNumber(inv != null ? inv.getReserved() : null);
        long disponible = Math.max(0L, onHand - reserved);
        return new DisponibilidadResponseDTO(varianteId, disponible);
    }

    @Override
    public ReservaStockResponseDTO reservarStock(ReservaStockRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        if (request.getVarianteId() == null) throw new IllegalArgumentException("varianteId es requerido");
        Variante v = varianteRepo.findById(request.getVarianteId())
            .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + request.getVarianteId()));
        InventarioVariante inv = repo.findByVarianteIdForUpdate(v.getId())
            .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + request.getVarianteId()));

        // si getOnHand()/getReserved() devuelven primitivos (long/int) no pueden ser null,
        // y si devuelven wrappers, safeNumber los normaliza.
        long onHand = safeNumber(inv != null ? inv.getOnHand() : null);
        long reserved = safeNumber(inv != null ? inv.getReserved() : null);
        long disponible = Math.max(0L, onHand - reserved);
        long qty = request.getCantidad();

        boolean ok = disponible >= qty && qty > 0;
        if (ok) {
            long newReserved = reserved + qty;
            // ajustar al tipo del campo en la entidad:
            // si reserved es Integer en la entidad: inv.setReserved((int)newReserved);
            // si reserved es Long/long en la entidad: inv.setReserved(newReserved);
            inv.setReserved((int) newReserved); // <-- adapta según el tipo real de tu entidad
            repo.save(inv);
            // crear movimiento de tipo RESERVA para trazabilidad
            try {
                MovimientoCrearDTO m = new MovimientoCrearDTO();
                Long productoId = v.getProducto() != null ? v.getProducto().getId() : null;
                m.setProductoId(productoId);
                m.setVarianteId(v.getId());
                m.setCantidad(qty);
                m.setTipo(com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario.RESERVA);
                m.setOrderRef(request.getOrderRef());
                m.setMetadataJson("{\"source\":\"reservarStock\"}");
                movimientoService.crear(m);
            } catch (Exception ex) {
                // no detener el flujo por fallo en trazabilidad; loguear si hay logger (omitido aquí)
            }
            disponible = Math.max(0L, onHand - newReserved);
        }
        return new ReservaStockResponseDTO(request.getVarianteId(), ok, disponible);
    }

    @Override
    public OperacionSimpleResponseDTO liberarReserva(LiberacionReservaRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        if (request.getOrderRef() == null) throw new IllegalArgumentException("orderRef es requerido");

        List<MovimientoInventario> reservas = movimientoRepo.findByOrderRefAndTipo(request.getOrderRef(), TipoMovimientoInventario.RESERVA);
        if (reservas == null || reservas.isEmpty()) {
            return new OperacionSimpleResponseDTO(false, "No se encontraron reservas para orderRef: " + request.getOrderRef());
        }

        for (MovimientoInventario mov : reservas) {
            if (mov.getVariante() == null) continue;
            Long varId = mov.getVariante().getId();
            // obtener la fila con lock para serializar confirmaciones concurrentes
            InventarioVariante inv = repo.findByVarianteIdForUpdate(varId).orElse(null);
            if (inv == null) continue;
            long reserved = safeNumber(inv.getReserved());
            long cantidad = safeNumber(mov.getCantidad());
            long newReserved = Math.max(0L, reserved - cantidad);
            inv.setReserved((int) newReserved);
            repo.save(inv);

            // registrar movimiento de liberación
            try {
                MovimientoCrearDTO m2 = new MovimientoCrearDTO();
                Long productoId = mov.getProducto() != null ? mov.getProducto().getId() : null;
                m2.setProductoId(productoId);
                m2.setVarianteId(varId);
                m2.setCantidad(cantidad);
                m2.setTipo(TipoMovimientoInventario.LIBERACION);
                m2.setOrderRef(request.getOrderRef());
                m2.setMetadataJson("{\"source\":\"liberarReserva\"}");
                movimientoService.crear(m2);
            } catch (Exception ex) {
                // ignorar error de trazabilidad
            }
        }

        return new OperacionSimpleResponseDTO(true, "Reservas liberadas para orderRef: " + request.getOrderRef());
    }

    @Override
    public OperacionSimpleResponseDTO confirmarVenta(ConfirmacionVentaRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("request null");
        if (request.getOrderRef() == null) throw new IllegalArgumentException("orderRef es requerido");

        List<MovimientoInventario> reservas = movimientoRepo.findByOrderRefAndTipo(request.getOrderRef(), TipoMovimientoInventario.RESERVA);
        if (reservas == null || reservas.isEmpty()) {
            return new OperacionSimpleResponseDTO(false, "No se encontraron reservas para orderRef: " + request.getOrderRef());
        }

        for (MovimientoInventario mov : reservas) {
            if (mov.getVariante() == null) continue;
            Long varId = mov.getVariante().getId();
            InventarioVariante inv = repo.findByVarianteIdForUpdate(varId).orElse(null);
            if (inv == null) continue;

            // Si ya existe una SALIDA_VENTA para esta orderRef y variante, omitir (idempotencia)
            boolean salidaExistente = movimientoRepo.findByOrderRefAndTipo(request.getOrderRef(), TipoMovimientoInventario.SALIDA_VENTA)
                    .stream().anyMatch(m -> m.getVariante() != null && m.getVariante().getId().equals(varId));
            if (salidaExistente) {
                continue; // ya fue consumida por otra confirmación
            }

            long onHand = safeNumber(inv.getOnHand());
            long reserved = safeNumber(inv.getReserved());
            long cantidad = safeNumber(mov.getCantidad());

            long newReserved = Math.max(0L, reserved - cantidad);
            long newOnHand = Math.max(0L, onHand - cantidad);

            // registrar movimiento de salida por venta PRIMERO (para que la inserción actúe como claim)
            boolean movimientoCreado = false;
            try {
                MovimientoCrearDTO m2 = new MovimientoCrearDTO();
                Long productoId = mov.getProducto() != null ? mov.getProducto().getId() : null;
                m2.setProductoId(productoId);
                m2.setVarianteId(varId);
                m2.setCantidad(cantidad);
                m2.setTipo(TipoMovimientoInventario.SALIDA_VENTA);
                m2.setOrderRef(request.getOrderRef());
                m2.setMetadataJson("{\"source\":\"confirmarVenta\"}");
                movimientoService.crear(m2);
                movimientoCreado = true;
            } catch (Exception ex) {
                // si falla la creación porque ya existe, asumimos que otra transacción hizo el trabajo
                movimientoCreado = movimientoRepo.findByOrderRefAndTipo(request.getOrderRef(), TipoMovimientoInventario.SALIDA_VENTA)
                        .stream().anyMatch(m -> m.getVariante() != null && m.getVariante().getId().equals(varId));
            }

            if (!movimientoCreado) {
                // otra transacción reclamó la salida; omitir
                continue;
            }

            // aplicar cambios al inventario solo si se creó el movimiento
            inv.setReserved((int) newReserved);
            inv.setOnHand((int) newOnHand);
            repo.save(inv);
        }

        return new OperacionSimpleResponseDTO(true, "Venta confirmada para orderRef: " + request.getOrderRef());
    }

    @Override
    public InventarioVarianteDTO ajustarStock(Long varianteId, long deltaOnHand, long deltaReserved) {
        InventarioVariante inv = repo.findByVarianteId(varianteId)
            .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado: " + varianteId));

        // para ajustarStock: usar safeNumber o asumir primitivo si la entidad ya lo usa
        long onHand = safeNumber(inv.getOnHand());
        long reserved = safeNumber(inv.getReserved());

        long newOnHand = Math.max(0L, onHand + deltaOnHand);
        long newReserved = Math.max(0L, reserved + deltaReserved);

        // adaptar cast según el tipo real en la entidad (Integer vs Long vs int)
        inv.setOnHand((int) newOnHand);
        inv.setReserved((int) newReserved);

        // Crear movimientos de inventario para trazabilidad (mismo contexto transaccional)
        Long productoId = null;
        if (inv.getVariante() != null && inv.getVariante().getProducto() != null) {
            productoId = inv.getVariante().getProducto().getId();
        }

        if (productoId != null && movimientoService != null) {
            if (deltaOnHand != 0) {
                MovimientoCrearDTO m = new MovimientoCrearDTO();
                m.setProductoId(productoId);
                m.setVarianteId(varianteId);
                m.setCantidad(Math.abs(deltaOnHand));
                m.setTipo(deltaOnHand > 0 ? TipoMovimientoInventario.ENTRADA_AJUSTE : TipoMovimientoInventario.SALIDA_VENTA);
                m.setMetadataJson("{\"source\":\"ajustarStock\",\"varianteId\":" + varianteId + "}");
                movimientoService.crear(m);
            }
            if (deltaReserved != 0) {
                MovimientoCrearDTO m2 = new MovimientoCrearDTO();
                m2.setProductoId(productoId);
                m2.setVarianteId(varianteId);
                m2.setCantidad(Math.abs(deltaReserved));
                m2.setTipo(deltaReserved > 0 ? TipoMovimientoInventario.RESERVA : TipoMovimientoInventario.LIBERACION);
                m2.setMetadataJson("{\"source\":\"ajustarStock\",\"varianteId\":" + varianteId + "}");
                movimientoService.crear(m2);
            }
        }

        InventarioVariante saved = repo.save(inv);
        return VarianteInventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioVarianteDTO> listarTodos() {
        return repo.findAll().stream()
                .map(VarianteInventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioVarianteDTO> listarBajoStock(Long threshold) {
        int th = threshold != null ? threshold.intValue() : 0;
        return repo.findAll().stream()
                .filter(i -> (i.getOnHand() != null ? i.getOnHand() : 0) < th)
                .map(VarianteInventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorVarianteId(Long varianteId) {
        repo.findByVarianteId(varianteId).ifPresent(inv -> {
            // realizar soft-delete si la entidad soporta deletedAt
            try {
                inv.setDeletedAt(LocalDateTime.now());
                repo.save(inv);
            } catch (Exception ex) {
                // fallback a borrado físico en caso de que la entidad no tenga deletedAt
                repo.delete(inv);
            }
        });
    }
}