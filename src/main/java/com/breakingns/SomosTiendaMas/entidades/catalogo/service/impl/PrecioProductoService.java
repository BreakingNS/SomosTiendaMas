package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.*;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.PrecioProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.PrecioProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrecioProductoService implements IPrecioProductoService {

    private final PrecioProductoRepository repo;
    private final ProductoRepository productoRepo;
    
    @Value("${app.iva.porcentaje:21}")
    private Integer defaultIvaPct;

    public PrecioProductoService(PrecioProductoRepository repo, ProductoRepository productoRepo) {
        this.repo = repo;
        this.productoRepo = productoRepo;
    }

    @Override
    public PrecioProductoResponseDTO crear(PrecioProductoCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getProductoId() == null) throw new IllegalArgumentException("productoId es requerido");

        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));

        PrecioProducto entidad = PrecioProductoMapper.fromCrear(dto);
        entidad = PrecioProductoMapper.fromCrearWithProducto(dto, producto);

        // calcular IVA y precioSinIva si no vienen
        Integer ivaPct = entidad.getIvaPorcentaje() != null ? entidad.getIvaPorcentaje() : this.defaultIvaPct;
        entidad.setIvaPorcentaje(ivaPct);
        if (entidad.getPrecioSinIvaCentavos() == null) {
            entidad.setPrecioSinIvaCentavos(calcularPrecioSinIvaCentavos(entidad.getMontoCentavos(), ivaPct));
        }

       // asegurar vigenciaDesde si no viene
        if (entidad.getVigenciaDesde() == null) {
            entidad.setVigenciaDesde(LocalDateTime.now());
        }

        // si el nuevo precio viene activo, desactivar/fechar otros activos del mismo producto
        if (Boolean.TRUE.equals(entidad.getActivo())) {
            List<PrecioProducto> activos = repo.findByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(dto.getProductoId());
            LocalDateTime now = LocalDateTime.now();
            for (PrecioProducto p : activos) {
                if (!p.getId().equals(entidad.getId())) {
                    p.setActivo(false);
                    // cerrar vigencia del anterior
                    if (p.getVigenciaHasta() == null) p.setVigenciaHasta(now);
                }
            }
            if (!activos.isEmpty()) repo.saveAll(activos);
        }

        PrecioProducto saved = repo.save(entidad);
        return PrecioProductoMapper.toResponse(saved);
    }

    @Override
    public PrecioProductoResponseDTO actualizar(Long id, PrecioProductoActualizarDTO dto) {
        PrecioProducto existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));

        boolean activating = dto.getActivo() != null && dto.getActivo() && !Boolean.TRUE.equals(existing.getActivo());
        // aplicar cambios básicos
        PrecioProductoMapper.applyActualizar(dto, existing);

        // determinar tasa de IVA a usar (dto > existente > default)
        Integer ivaPct = dto.getIvaPorcentaje() != null ? dto.getIvaPorcentaje()
                : (existing.getIvaPorcentaje() != null ? existing.getIvaPorcentaje() : this.defaultIvaPct);
        existing.setIvaPorcentaje(ivaPct);

        // calcular precioSinIva si quedó nulo
        if (dto.getPrecioSinIvaCentavos() == null) {
            Long monto = dto.getMontoCentavos() != null ? dto.getMontoCentavos() : existing.getMontoCentavos();
            if (monto != null) {
                existing.setPrecioSinIvaCentavos(calcularPrecioSinIvaCentavos(monto, ivaPct));
            }
        }

        // si se activa este precio, desactivar los demás activos para el mismo producto
        if (activating && existing.getProducto() != null) {
            List<PrecioProducto> activos = repo.findByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(existing.getProducto().getId());
            if (!activos.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                for (PrecioProducto p : activos) {
                    if (!p.getId().equals(existing.getId())) {
                        p.setActivo(false);
                      if (p.getVigenciaHasta() == null) p.setVigenciaHasta(now);
                    }
                }
                repo.saveAll(activos);
            }
        }

        PrecioProducto updated = repo.save(existing);
        return PrecioProductoMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioProductoResponseDTO obtenerPorId(Long id) {
        PrecioProducto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
        if (p.getDeletedAt() != null) throw new EntityNotFoundException("Precio eliminado: " + id);
        return PrecioProductoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioProductoResponseDTO obtenerVigentePorProductoId(Long productoId) {
        return repo.findFirstByProductoIdAndActivoTrueOrderByVigenciaDesdeDesc(productoId)
                .map(PrecioProductoMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioProductoResponseDTO> listarPorProductoId(Long productoId) {
        List<PrecioProducto> list = repo.findByProductoIdOrderByVigenciaDesdeDesc(productoId);
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioProductoResponseDTO> buscarVigentesPorProductoIdEnFecha(Long productoId, LocalDateTime fecha) {
        if (fecha == null) fecha = LocalDateTime.now();
        List<PrecioProducto> list = repo.findByProductoIdAndVigenciaDesdeLessThanEqualAndVigenciaHastaGreaterThanEqual(productoId, fecha, fecha);
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioProductoResponseDTO> listarActivas() {
        List<PrecioProducto> list = repo.findAllByDeletedAtIsNull();
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        PrecioProducto p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
        p.setDeletedAt(LocalDateTime.now());
        repo.save(p);
    }

    public static Long calcularPrecioSinIvaCentavos(Long montoCentavos, Integer ivaPct) {
        if (montoCentavos == null || ivaPct == null) return null;
        BigDecimal monto = BigDecimal.valueOf(montoCentavos);
        BigDecimal divisor = BigDecimal.ONE.add(BigDecimal.valueOf(ivaPct).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_EVEN));
        // Dividir y redondear al entero más cercano (centavos)
        BigDecimal neto = monto.divide(divisor, 0, RoundingMode.HALF_EVEN);
        return neto.longValue();
    }
}
