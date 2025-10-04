package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteValoresAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante.VarianteValoresResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VarianteProductoMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteOpcionValorRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class VarianteController {

    private final IVarianteProductoService varianteService;
    private final ProductoRepository productoRepository;
    private final VarianteOpcionValorRepository vovRepository;

    public VarianteController(IVarianteProductoService varianteService,
                              ProductoRepository productoRepository,
                              VarianteOpcionValorRepository vovRepository) {
        this.varianteService = varianteService;
        this.productoRepository = productoRepository;
        this.vovRepository = vovRepository;
    }

    @PostMapping("/variantes")
    public ResponseEntity<VarianteResponseDTO> crear(@RequestBody @Valid VarianteCrearDTO dto) {
        Producto p = productoRepository.findById(dto.getProductoId()).orElseThrow();
        VarianteProducto v = VarianteProductoMapper.toEntity(dto, p);
        VarianteProducto creado = varianteService.crear(dto.getProductoId(), v);
        return ResponseEntity.ok(VarianteProductoMapper.toResponse(creado));
    }

    @PutMapping("/variantes/{id}")
    public ResponseEntity<VarianteResponseDTO> actualizar(@PathVariable Long id, @RequestBody @Valid VarianteActualizarDTO dto) {
        VarianteProducto cambios = new VarianteProducto();
        cambios.setSku(dto.getSku());
        cambios.setCodigoBarras(dto.getCodigoBarras());
        cambios.setPesoGramos(dto.getPesoGramos());
        cambios.setAltoMm(dto.getAltoMm());
        cambios.setAnchoMm(dto.getAnchoMm());
        cambios.setLargoMm(dto.getLargoMm());
        cambios.setMetadataJson(dto.getMetadataJson());
        VarianteProducto actualizado = varianteService.actualizar(id, cambios);
        return ResponseEntity.ok(VarianteProductoMapper.toResponse(actualizado));
    }

    @GetMapping("/variantes/{id}")
    public ResponseEntity<VarianteResponseDTO> obtener(@PathVariable Long id) {
        VarianteProducto v = varianteService.obtener(id).orElseThrow();
        return ResponseEntity.ok(VarianteProductoMapper.toResponse(v));
    }

    @GetMapping("/productos/{productoId}/variantes")
    public ResponseEntity<List<VarianteResponseDTO>> listarPorProducto(@PathVariable Long productoId) {
        List<VarianteResponseDTO> list = varianteService.listarPorProducto(productoId).stream()
                .map(VarianteProductoMapper::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/variantes/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam String usuario) {
        varianteService.eliminarLogico(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/variantes/{id}/valores")
    public ResponseEntity<VarianteValoresResponseDTO> asignarValores(@PathVariable Long id,
                                                                     @RequestBody @Valid VarianteValoresAsignarDTO dto) {
        varianteService.asignarValores(id, dto.getValorIds());
        List<Long> asignados = vovRepository.findByVariante(varianteService.obtener(id).orElseThrow())
                .stream().map(v -> v.getValor().getId()).toList();
        return ResponseEntity.ok(new VarianteValoresResponseDTO(id, asignados));
    }
}
