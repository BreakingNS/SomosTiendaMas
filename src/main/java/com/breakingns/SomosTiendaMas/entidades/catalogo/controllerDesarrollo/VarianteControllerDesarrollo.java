package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteActualizarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteOpcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/dev/api/variantes")
public class VarianteControllerDesarrollo {

    private final IVarianteService service;
    private final IPrecioVarianteService precioService;
    private final IVarianteFisicoService fisicoService;
    private final IVarianteOpcionService opcionService;

    public VarianteControllerDesarrollo(
            IVarianteService service,
            IPrecioVarianteService precioService,
            IVarianteFisicoService fisicoService,
            IVarianteOpcionService opcionService) {
        this.service = service;
        this.precioService = precioService;
        this.fisicoService = fisicoService;
        this.opcionService = opcionService;
    }

    @PostMapping
    public ResponseEntity<VarianteDTO> crear(@RequestBody VarianteCrearDTO dto) {
        VarianteDTO created = service.crearVariante(dto);
        URI loc = URI.create("/dev/api/variantes/" + created.getId());
        return ResponseEntity.created(loc).body(created);
    }

    @GetMapping
    public ResponseEntity<List<VarianteDTO>> listarTodas() {
        List<VarianteDTO> list = service.listarTodas();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VarianteDTO> obtener(@PathVariable Long id) {
        VarianteDTO dto = service.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<java.util.List<VarianteDTO>> listarPorProducto(@PathVariable Long productoId) {
        java.util.List<VarianteDTO> list = service.listarPorProductoId(productoId);
        return ResponseEntity.ok(list);
    }

    // PUT: actualizar metadatos basicos
    @PutMapping("/{id}")
    public ResponseEntity<VarianteDTO> actualizar(@PathVariable Long id, @RequestBody VarianteCrearDTO dto) {
        VarianteDTO updated = service.actualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // REVIEW: modificar para que solo lo usen los admins
    // Endpoint temporal para borrado físico (uso solo para pruebas; restringir a admins luego)
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> eliminarPermanente(@PathVariable Long id) {
        try {
            service.eliminarPermanente(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ENDPOINTS ESPECÍFICOS ==========

    /**
     * Actualiza el precio de una variante
     * PATCH /dev/api/variantes/{id}/precio
     * Body: { "montoCentavos": 12990, "vigenciaDesde": "2026-01-27T10:00:00", ... }
     * Validación: precio > 0
     */
    @PatchMapping("/{id}/precio")
    public ResponseEntity<PrecioVarianteResponseDTO> actualizarPrecio(
            @PathVariable Long id,
            @RequestBody PrecioVarianteActualizarDTO dto) {
        try {
            // Obtener el precio vigente actual de la variante
            PrecioVarianteResponseDTO precioActual = precioService.obtenerVigentePorVarianteId(id);
            
            if (precioActual == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Actualizar el precio vigente
            PrecioVarianteResponseDTO updated = precioService.actualizar(precioActual.getId(), dto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza las propiedades físicas de una variante
     * PATCH /dev/api/variantes/{id}/fisicas
     * Body: { "weightGrams": 2400, "widthMm": 350, "heightMm": 25, "depthMm": 245, ... }
     * Validación: valores numéricos positivos
     */
    @PatchMapping("/{id}/fisicas")
    public ResponseEntity<PhysicalPropertiesDTO> actualizarFisicas(
            @PathVariable Long id,
            @RequestBody PhysicalPropertiesDTO dto) {
        try {
            // Verificar que la variante existe
            service.obtenerPorId(id);
            
            // Crear o actualizar propiedades físicas
            PhysicalPropertiesDTO updated = fisicoService.crearOActualizarPorVariante(id, dto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene las opciones y sus valores asociadas a una variante
     * GET /dev/api/variantes/{id}/opciones
     * Response: { "varianteId": 1, "opciones": [{"id": 1, "nombre": "Color", "valores": [...]}, ...] }
     */
    @GetMapping("/{id}/opciones")
    public ResponseEntity<VarianteConOpcionesValoresDTO> obtenerOpciones(@PathVariable Long id) {
        try {
            VarianteConOpcionesValoresDTO dto = opcionService.obtenerVarianteConOpcionesConValores(id);
            return ResponseEntity.ok(dto);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza las opciones y valires de una variante
     * PUT /dev/api/variantes/{id}/opciones
     * Body: { "varianteId": 1, "opcionesIds": [1, 2, 3], "valores": {"Color": "Azul", "Memoria RAM": "8 GB"} }
     * Validación: opciones deben existir en el catálogo
     */
    @PutMapping("/{id}/opciones")
    public ResponseEntity<Void> actualizarOpciones(
            @PathVariable Long id,
            @RequestBody com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesModificarDTO dto,
            @RequestParam(value = "asDefault", required = false, defaultValue = "false") boolean asDefault) {
        try {
            // Verificar que la variante existe
            var variante = service.obtenerPorId(id);
            if (asDefault) {
                // aplicar cambios a las opciones por defecto del producto
                opcionService.modificarOpcionesPorProducto(variante.getProductoId(), dto, "system");
            } else {
                // Modificar opciones y/o valores a nivel de variante (overrides)
                opcionService.modificarOpciones(id, dto, "system");
            }
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
    // batch create for product
    @PostMapping("/productos/{productoId}/variantes/batch")
    public ResponseEntity<List<VarianteDTO>> crearBatch(@PathVariable Long productoId, @RequestBody List<VarianteCrearDTO> dtos) {
        List<VarianteDTO> created = service.crearVariantesBatch(productoId, dtos);
        return ResponseEntity.ok(created);
    }*/
}
