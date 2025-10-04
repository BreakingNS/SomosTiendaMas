package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoPrecio;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.PrecioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.PrecioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PrecioVarianteServiceImpl implements IPrecioVarianteService {

    private final PrecioVarianteRepository precioRepo;
    private final VarianteProductoRepository varianteRepo;

    public PrecioVarianteServiceImpl(PrecioVarianteRepository precioRepo,
                                     VarianteProductoRepository varianteRepo) {
        this.precioRepo = precioRepo;
        this.varianteRepo = varianteRepo;
    }

    @Override
    public PrecioVariante setPrecioLista(Long varianteId, Long montoCentavos) {
        VarianteProducto v = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));

        // Desactivar precios activos
        precioRepo.findByVarianteAndActivoTrue(v)
                .forEach(p -> { p.setActivo(false); precioRepo.save(p); });

        // Crear nuevo precio
        PrecioVariante nuevo = new PrecioVariante();
        nuevo.setVariante(v);
        nuevo.setMontoCentavos(montoCentavos);
        nuevo.setTipo(TipoPrecio.LISTA);
        nuevo.setActivo(true);
        nuevo.setVigenciaDesde(LocalDateTime.now());
        return precioRepo.save(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrecioVariante> obtenerPrecioVigente(Long varianteId) {
        VarianteProducto v = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new IllegalArgumentException("Variante no encontrada: " + varianteId));
        return precioRepo.findPrecioVigente(v, LocalDateTime.now());
    }
}
