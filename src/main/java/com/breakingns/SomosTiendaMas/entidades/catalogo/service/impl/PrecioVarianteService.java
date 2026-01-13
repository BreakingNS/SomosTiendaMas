package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.PrecioProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.PrecioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
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
public class PrecioVarianteService implements IPrecioVarianteService {

    private final PrecioVarianteRepository repo;
    private final VarianteRepository varianteRepo;
    
    @Value("${app.iva.porcentaje:21}")
    private Integer defaultIvaPct;

    public PrecioVarianteService(PrecioVarianteRepository repo, VarianteRepository varianteRepo) {
        this.repo = repo;
        this.varianteRepo = varianteRepo;
    }

    @Override
    public PrecioVarianteResponseDTO crear(PrecioVarianteCrearDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        if (dto.getVarianteId() == null) throw new IllegalArgumentException("varianteId es requerido");

        Variante variante = varianteRepo.findById(dto.getVarianteId())
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada: " + dto.getVarianteId()));

        PrecioVariante entidad = PrecioProductoMapper.fromCrear(dto);
        // asignar producto y variante desde la variante encontrada
        entidad.setProducto(variante.getProducto());
        entidad.setVariante(variante);

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
            List<PrecioVariante> activos = repo.findByVarianteIdAndActivoTrueOrderByVigenciaDesdeDesc(dto.getVarianteId());
            LocalDateTime now = LocalDateTime.now();
            for (PrecioVariante p : activos) {
                if (!p.getId().equals(entidad.getId())) {
                    p.setActivo(false);
                    // cerrar vigencia del anterior
                    if (p.getVigenciaHasta() == null) p.setVigenciaHasta(now);
                }
            }
            if (!activos.isEmpty()) repo.saveAll(activos);
        }

        PrecioVariante saved = repo.save(entidad);
        return PrecioProductoMapper.toResponse(saved);
    }

    @Override
    public PrecioVarianteResponseDTO actualizar(Long id, PrecioVarianteActualizarDTO dto) {
        PrecioVariante existing = repo.findById(id)
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
        if (activating && existing.getVariante() != null) {
            List<PrecioVariante> activos = repo.findByVarianteIdAndActivoTrueOrderByVigenciaDesdeDesc(existing.getVariante().getId());
            if (!activos.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                for (PrecioVariante p : activos) {
                    if (!p.getId().equals(existing.getId())) {
                        p.setActivo(false);
                      if (p.getVigenciaHasta() == null) p.setVigenciaHasta(now);
                    }
                }
                repo.saveAll(activos);
            }
        }

        PrecioVariante updated = repo.save(existing);
        return PrecioProductoMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioVarianteResponseDTO obtenerPorId(Long id) {
        PrecioVariante p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
        if (p.getDeletedAt() != null) throw new EntityNotFoundException("Precio eliminado: " + id);
        return PrecioProductoMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioVarianteResponseDTO obtenerVigentePorVarianteId(Long varianteId) {
        return repo.findFirstByVarianteIdAndActivoTrueOrderByVigenciaDesdeDesc(varianteId)
                .map(PrecioProductoMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioVarianteResponseDTO> listarPorVarianteId(Long varianteId) {
        List<PrecioVariante> list = repo.findByVarianteIdOrderByVigenciaDesdeDesc(varianteId);
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioVarianteResponseDTO> buscarVigentesPorVarianteIdEnFecha(Long varianteId, LocalDateTime fecha) {
        if (fecha == null) fecha = LocalDateTime.now();
        List<PrecioVariante> list = repo.findByVarianteIdAndVigenciaDesdeLessThanEqualAndVigenciaHastaGreaterThanEqual(varianteId, fecha, fecha);
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecioVarianteResponseDTO> listarActivas() {
        List<PrecioVariante> list = repo.findAllByDeletedAtIsNull();
        return list.stream().map(PrecioProductoMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        PrecioVariante p = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Precio no encontrado: " + id));
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