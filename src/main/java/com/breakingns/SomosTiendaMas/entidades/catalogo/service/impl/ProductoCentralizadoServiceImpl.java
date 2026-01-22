/* NOTE: lo desarrollado en ProductoCentralizadoServiceImplV2 ira aqui una vez finalizado y probado completamente.
package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteOpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteListaDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteAnidadaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoCentralizadoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Transactional
public class ProductoCentralizadoServiceImpl implements IProductoCentralizadoService {

	private static final Logger log = LoggerFactory.getLogger(ProductoCentralizadoServiceImpl.class);

	private final IProductoService productoService;
	private final IVarianteOpcionService varianteOpcionService;
	private final IVarianteFisicoService varianteFisicoService;
	private final IPrecioVarianteService precioVarianteService;
	private final IInventarioVarianteService inventarioVarianteService;
	private final IImagenVarianteService imagenVarianteService;
	private final IVarianteService varianteService;
	private final VarianteOpcionRepository varianteOpcionRepository;
	private final VarianteRepository varianteRepository;
	private final OpcionRepository opcionRepository;

	public ProductoCentralizadoServiceImpl(IProductoService productoService,
											IVarianteOpcionService varianteOpcionService,
											IVarianteFisicoService varianteFisicoService,
											IPrecioVarianteService precioVarianteService,
											IInventarioVarianteService inventarioVarianteService,
											IImagenVarianteService imagenVarianteService,
											IVarianteService varianteService,
											VarianteOpcionRepository varianteOpcionRepository,
											VarianteRepository varianteRepository,
											OpcionRepository opcionRepository) {
		this.productoService = productoService;
		this.varianteOpcionService = varianteOpcionService;
		this.varianteFisicoService = varianteFisicoService;
		this.precioVarianteService = precioVarianteService;
		this.inventarioVarianteService = inventarioVarianteService;
		this.imagenVarianteService = imagenVarianteService;
		this.varianteService = varianteService;
		this.varianteOpcionRepository = varianteOpcionRepository;
		this.varianteRepository = varianteRepository;
		this.opcionRepository = opcionRepository;
	}

	@Override
	public ProductoCentralizadoResponseFullDTO crear(ProductoCentralizadoCrearDTO dto) {
		return crear(dto, "system");
	}

	@Override
	public ProductoCentralizadoResponseFullDTO crear(ProductoCentralizadoCrearDTO dto, String usuario) {
		if (dto == null) throw new IllegalArgumentException("payload required");

		ProductoCrearDTO pDto = dto.getProducto();
		if (pDto == null) throw new IllegalArgumentException("producto required");

		// crear producto (ProductoService.crear maneja varianteDefault si viene en producto DTO)
		ProductoCentralizadoResponseDTO created = productoService.crear(pDto);

		// Nota: las opciones ahora se persistirán a nivel de variante.
		// No se crean registros en `producto_opcion` (deprecated).
		// La asignación de opciones por variante se realiza más abajo, después de crear las variantes.

		// crear variantes (nueva forma obligatoria)
		List<VarianteAnidadaCrearDTO> variantes = dto.getVariantes();
		if (variantes == null || variantes.isEmpty()) {
			throw new IllegalArgumentException("variantes is required and must contain at least one VarianteAnidadaCrearDTO");
		}

		// convertir a VarianteCrearDTO y crear en batch
		java.util.List<com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO> flat = new java.util.ArrayList<>();
		for (VarianteAnidadaCrearDTO v : variantes) flat.add(v.toFlatVariante());
		java.util.List<VarianteDTO> createdVars = varianteService.crearVariantesBatch(created.getId(), flat);

		// iterar por índice y persistir sub-recursos usando el id creado
		for (int i = 0; i < variantes.size(); i++) {
			VarianteAnidadaCrearDTO nested = variantes.get(i);
			VarianteDTO createdVar = createdVars.get(i);
			Long vid = createdVar.getId();

			// opciones de variante: crear relaciones VarianteOpcion en batch y luego asignar valores
			List<VarianteOpcionesAsignarDTO> vops = nested.getVarianteOpciones();
			if (vops != null) {
				List<VarianteOpcion> toSaveVos = new java.util.ArrayList<>();
				for (VarianteOpcionesAsignarDTO vo : vops) {
					if (vo.varianteId == null) vo.varianteId = vid;
					if (vo.opciones != null) {
						for (var oSel : vo.opciones) {
							Long opcionId = oSel.opcionId;
							if (opcionId == null) continue;
							boolean existe = varianteOpcionRepository.existsByVariante_Producto_IdAndOpcion_IdAndDeletedAtIsNull(created.getId(), opcionId);
							if (!existe) {
								Opcion opcion = opcionRepository.findById(opcionId).orElse(null);
								if (opcion == null) {
									log.warn("Opcion id {} no encontrada al crear VarianteOpcion para producto {}", opcionId, created.getId());
									continue;
								}
								Variante vEntity = varianteRepository.findById(vid).orElse(null);
								if (vEntity == null) {
									log.warn("Variante id {} no encontrada al crear VarianteOpcion", vid);
									continue;
								}
								VarianteOpcion voEntity = new VarianteOpcion();
								voEntity.setVariante(vEntity);
								voEntity.setOpcion(opcion);
								voEntity.setOrden(oSel.orden != null ? oSel.orden : 0);
								voEntity.setRequerido(oSel.requerido != null ? oSel.requerido : false);
								voEntity.setActivo(oSel.activo != null ? oSel.activo : true);
								toSaveVos.add(voEntity);
							}
						}
					}
				// save all nuevas relaciones de opcion
				if (!toSaveVos.isEmpty()) {
					varianteOpcionRepository.saveAll(toSaveVos);
				}
				// ahora asignar los valores (VarianteValor) - cada vo puede contener varios valores
				for (VarianteOpcionesAsignarDTO vo : vops) {
					try {
						varianteOpcionService.asignarOpciones(vo, usuario);
					} catch (Exception e) {
						log.warn("Error asignando opciones para variante {}: {}", vid, e.getMessage());
					}
				}
			}

			// physical
			List<PhysicalPropertiesDTO> phys = nested.getPhysical();
			if (phys != null && !phys.isEmpty()) {
				varianteFisicoService.crearOActualizarPorVariante(vid, phys.get(0));
			}

			// precios
			List<PrecioVarianteCrearDTO> preciosNested = nested.getPrecios();
			if (preciosNested != null) {
				for (PrecioVarianteCrearDTO p : preciosNested) {
					if (p.getVarianteId() == null) p.setVarianteId(vid);
					precioVarianteService.crear(p);
				}
			}

			// inventarios
			List<InventarioVarianteDTO> invs = nested.getInventarios();
			if (invs != null) {
				for (InventarioVarianteDTO inv : invs) {
					if (inv.getVarianteId() == null) inv.setVarianteId(vid);
					inventarioVarianteService.crear(inv);
				}
			}

			// imagenes
			List<ImagenVarianteDTO> imgs = nested.getImagenes();
			if (imgs != null) {
				for (ImagenVarianteDTO img : imgs) {
					if (img.getVarianteId() == null) img.setVarianteId(vid);
					imagenVarianteService.crear(img);
				}
			}
		}

		// construir respuesta completa con variantes y sub-recursos (similar al payload enviado)
		ProductoCentralizadoResponseDTO prodFull = productoService.obtenerPorId(created.getId());

		com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullDTO full = new com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullDTO();
		full.setProducto(prodFull);

		List<VarianteCentralizadaResponseDTO> variantesResp = new ArrayList<>();
		// usar las variantes creadas (createdVars) como fuente de verdad para construir la respuesta
		for (VarianteDTO v : createdVars) {
			VarianteCentralizadaResponseDTO vr = new VarianteCentralizadaResponseDTO();
			vr.setId(v.getId());
			vr.setSku(v.getSku());
			vr.setAttributesJson(v.getAttributesJson());
			vr.setAttributesHash(v.getAttributesHash());
			vr.setEsDefault(v.getEsDefault());
			vr.setActivo(v.getActivo());

			// precios
			try {
				var precios = precioVarianteService.listarPorVarianteId(v.getId());
				vr.setPrecios(precios != null ? precios : new ArrayList<>());
			} catch (Exception e) {
				log.warn("Error al obtener precios para variante {}: {}", v.getId(), e.getMessage());
				vr.setPrecios(new ArrayList<>());
			}
			// inventarios
			try {
				var invs = inventarioVarianteService.listarPorVarianteId(v.getId());
				vr.setInventarios(invs != null ? invs : new ArrayList<>());
			} catch (Exception e) {
				log.warn("Error al obtener inventarios para variante {}: {}", v.getId(), e.getMessage());
				vr.setInventarios(new ArrayList<>());
			}
			// imagenes
			try {
				var imgs = imagenVarianteService.listarPorVarianteId(v.getId());
				vr.setImagenes(imgs != null ? imgs : new ArrayList<>());
			} catch (Exception e) {
				log.warn("Error al obtener imagenes para variante {}: {}", v.getId(), e.getMessage());
				vr.setImagenes(new ArrayList<>());
			}
			// physical
			try {
				var phys = varianteFisicoService.obtenerPorVarianteId(v.getId());
				vr.setPhysical(phys != null ? List.of(phys) : new ArrayList<>());
			} catch (Exception e) {
				log.warn("Error al obtener physical para variante {}: {}", v.getId(), e.getMessage());
				vr.setPhysical(new ArrayList<>());
			}
			// opciones con valores
			try {
				var opc = varianteOpcionService.obtenerVarianteConOpcionesConValores(v.getId());
				vr.setVarianteOpciones(opc != null ? opc : new com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesValoresDTO(v.getId(), new ArrayList<>()));
			} catch (Exception e) {
				log.warn("Error al obtener opciones para variante {}: {}", v.getId(), e.getMessage());
				vr.setVarianteOpciones(new com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesValoresDTO(v.getId(), new ArrayList<>()));
			}

			variantesResp.add(vr);
		}

		full.setVariantes(variantesResp);

		return full;
	}

	private Long findDefaultVarianteId(ProductoCentralizadoResponseDTO created) {
		if (created == null) return null;
		List<VarianteListaDTO> list = created.getVariantes();
		if (list == null || list.isEmpty()) return null;
		for (VarianteListaDTO v : list) {
			if (Boolean.TRUE.equals(v.getEsDefault())) return v.getId();
		}
		// fallback: devolver la primera
		return list.get(0).getId();
	}
}*/