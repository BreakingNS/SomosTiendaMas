package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.InventarioMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioVarianteService implements IInventarioVarianteService {

    private final InventarioVarianteRepository repo;
    private final VarianteRepository varianteRepo;

    public InventarioVarianteService(InventarioVarianteRepository repo, VarianteRepository varianteRepo) {
        this.repo = repo;
        this.varianteRepo = varianteRepo;
    }

    @Override
    public InventarioVarianteDTO crear(InventarioVarianteDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto null");
        if (dto.getVarianteId() == null) throw new IllegalArgumentException("varianteId es requerido");

        // asociar inventario a la variante default del Variante
        Variante variante = varianteRepo.findById(dto.getVarianteId())
            .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + dto.getVarianteId()));

        InventarioVariante entidad = InventarioMapper.fromDto(dto);
        entidad.setVariante(variante);
        InventarioVariante saved = repo.save(entidad);
        return InventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventarioVarianteDTO obtenerPorVarianteId(Long varianteId) {
        if (varianteId == null) throw new IllegalArgumentException("varianteId es requerido");
        InventarioVariante inv = repo.findByVarianteId(varianteId)
            .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para variante: " + varianteId));
        return InventarioMapper.toDto(inv);
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
        Variante v = varianteRepo.findById(request.getVarianteId())
            .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + request.getVarianteId()));
        InventarioVariante inv = repo.findByVarianteId(v.getId())
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
            disponible = Math.max(0L, onHand - newReserved);
        }
        return new ReservaStockResponseDTO(request.getVarianteId(), ok, disponible);
    }

    @Override
    public OperacionSimpleResponseDTO liberarReserva(LiberacionReservaRequestDTO request) {
        throw new UnsupportedOperationException("Liberación por orderRef no implementada en este servicio. Usa MovimientoInventarioService o adapta repo.");
    }

    @Override
    public OperacionSimpleResponseDTO confirmarVenta(ConfirmacionVentaRequestDTO request) {
        throw new UnsupportedOperationException("Confirmación de venta por orderRef no implementada en este servicio. Usa MovimientoInventarioService o adapta repo.");
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

        InventarioVariante saved = repo.save(inv);
        return InventarioMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioVarianteDTO> listarTodos() {
        return repo.findAll().stream()
                .map(InventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioVarianteDTO> listarBajoStock(Long threshold) {
        int th = threshold != null ? threshold.intValue() : 0;
        return repo.findAll().stream()
                .filter(i -> (i.getOnHand() != null ? i.getOnHand() : 0) < th)
                .map(InventarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorVarianteId(Long varianteId) {
        repo.findByVarianteId(varianteId).ifPresent(inv -> {
            // si querés soft-delete y tu entidad tiene deletedAt, setearlo aquí.
            // Si no, borramos físicamente:
            repo.delete(inv);
        });
    }
}