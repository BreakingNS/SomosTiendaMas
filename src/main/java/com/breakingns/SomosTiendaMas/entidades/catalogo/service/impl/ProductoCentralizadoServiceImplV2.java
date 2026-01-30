package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteCentralizadaResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteAnidadaCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.VarianteMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.InventarioVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.VarianteConOpcionesValoresDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoCentralizadoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IPrecioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IInventarioVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenVarianteService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteOpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class ProductoCentralizadoServiceImplV2 implements IProductoCentralizadoService {

	private static final Logger log = LoggerFactory.getLogger(ProductoCentralizadoServiceImplV2.class);

	private final IProductoService productoService;
	private final IVarianteService varianteService;
	private final IPrecioVarianteService precioVarianteService;
	private final IInventarioVarianteService inventarioVarianteService;
	private final IImagenVarianteService imagenVarianteService;
	private final IVarianteFisicoService varianteFisicoService;
	private final IVarianteOpcionService varianteOpcionService;
	private final VarianteOpcionRepository varianteOpcionRepository;
	private final VarianteRepository varianteRepository;
	private final OpcionRepository opcionRepository;

	public ProductoCentralizadoServiceImplV2(
			IProductoService productoService, 
			IVarianteService varianteService,
			IPrecioVarianteService precioVarianteService,
			IInventarioVarianteService inventarioVarianteService,
			IImagenVarianteService imagenVarianteService,
			IVarianteFisicoService varianteFisicoService,
			IVarianteOpcionService varianteOpcionService,
			VarianteOpcionRepository varianteOpcionRepository,
			VarianteRepository varianteRepository,
			OpcionRepository opcionRepository) {
		this.productoService = productoService;
		this.varianteService = varianteService;
		this.precioVarianteService = precioVarianteService;
		this.inventarioVarianteService = inventarioVarianteService;
		this.imagenVarianteService = imagenVarianteService;
		this.varianteFisicoService = varianteFisicoService;
		this.varianteOpcionService = varianteOpcionService;
		this.varianteOpcionRepository = varianteOpcionRepository;
		this.varianteRepository = varianteRepository;
		this.opcionRepository = opcionRepository;
	}

	@Override
	@Transactional
	public ProductoCentralizadoResponseFullDTO crear(ProductoCentralizadoCrearDTO dto) {
		return crear(dto, "system");
	}

	@Override
	public ProductoCentralizadoResponseFullDTO crear(ProductoCentralizadoCrearDTO dto, String usuario) {
		log.info("[V2] crear llamado por usuario='{}'", usuario);
		if (dto == null) throw new IllegalArgumentException("dto es requerido");

		// 1) crear producto SIN variante default (V2 las crea después)
		var createdProducto = productoService.crearSoloProducto(dto.getProducto());
		log.debug("[V2] Producto creado con ID={}", createdProducto.getId());

		// 2) preparar variantes planas y crear en batch
		List<VarianteCrearDTO> flats = new ArrayList<>();
		if (dto.getVariantes() != null) {
			for (VarianteAnidadaCrearDTO v : dto.getVariantes()) {
				if (v == null) continue;
				flats.add(v.toFlatVariante());
			}
		}

		List<VarianteDTO> createdVars = varianteService.crearVariantesBatch(createdProducto.getId(), flats);
		log.debug("[V2] {} variantes creadas", createdVars.size());

		// 2.5) Crear relaciones VarianteOpcion antes de asignar valores
		crearRelacionesVarianteOpcion(createdProducto.getId(), dto, createdVars);

		// 3) procesar sub-recursos por variante
		List<VarianteCentralizadaResponseDTO> variantesResp = new ArrayList<>();
		
		for (int i = 0; i < createdVars.size(); i++) {
			VarianteDTO vd = createdVars.get(i);
			VarianteAnidadaCrearDTO vPayload = (dto.getVariantes() != null && i < dto.getVariantes().size()) 
				? dto.getVariantes().get(i) 
				: null;
			
			VarianteCentralizadaResponseDTO vr = new VarianteCentralizadaResponseDTO();
			vr.setId(vd.getId());
			vr.setSku(vd.getSku());
			vr.setAttributesJson(vd.getAttributesJson());
			vr.setAttributesHash(vd.getAttributesHash());
			vr.setEsDefault(vd.getEsDefault());
			vr.setActivo(vd.getActivo());

			if (vPayload != null) {
				// 3.1) Opciones y valores
				if (vPayload.getVarianteOpciones() != null && !vPayload.getVarianteOpciones().isEmpty()) {
					for (VarianteOpcionesAsignarDTO asignar : vPayload.getVarianteOpciones()) {
						if (asignar != null) {
							asignar.varianteId = vd.getId(); // setear el ID de la variante creada
							varianteOpcionService.asignarOpciones(asignar, usuario);
						}
					}
					VarianteConOpcionesValoresDTO opciones = varianteOpcionService.obtenerVarianteConOpcionesConValores(vd.getId());
					// construir attributesJson a partir de opciones (solo nombre -> valores)
					try {
						if (opciones != null && opciones.getOpciones() != null && !opciones.getOpciones().isEmpty()) {
							Map<String, Object> map = new LinkedHashMap<>();
							for (var o : opciones.getOpciones()) {
								if (o == null) continue;
								var nombre = o.getNombre();
								if (nombre == null) continue;
								if (o.getValores() == null || o.getValores().isEmpty()) {
									map.put(nombre, new ArrayList<>());
								} else if (o.getValores().size() == 1) {
									map.put(nombre, o.getValores().get(0).getValor());
								} else {
									java.util.List<String> vals = new java.util.ArrayList<>();
									for (var vv : o.getValores()) {
										vals.add(vv.getValor());
									}
									map.put(nombre, vals);
								}
							}
							ObjectMapper mapper = new ObjectMapper();
							String attrsJson = mapper.writeValueAsString(map);
							String hash = sha256Hex(attrsJson);
							// persistir en variante mediante el servicio de variantes
							try {
								VarianteCrearDTO update = new VarianteCrearDTO();
								update.setAttributesJson(attrsJson);
								update.setAttributesHash(hash);
								varianteService.actualizar(vd.getId(), update);
								// actualizar el objeto local vd para reflejar cambios en la respuesta
								vd.setAttributesJson(attrsJson);
								vd.setAttributesHash(hash);
								vr.setAttributesJson(attrsJson);
								vr.setAttributesHash(hash);
							} catch (Exception ex) {
								log.warn("[V2] No se pudo actualizar attributes para variante {}: {}", vd.getId(), ex.getMessage());
							}
						}
					} catch (Exception ex) {
						log.warn("[V2] Error construyendo attributesJson para variante {}: {}", vd.getId(), ex.getMessage());
					}
					vr.setVarianteOpciones(opciones != null ? opciones : new VarianteConOpcionesValoresDTO(vd.getId(), new ArrayList<>()));
				} else {
					vr.setVarianteOpciones(new VarianteConOpcionesValoresDTO(vd.getId(), new ArrayList<>()));
				}

				// 3.2) Precios
				List<PrecioVarianteResponseDTO> preciosCreados = new ArrayList<>();
				if (vPayload.getPrecios() != null) {
					for (PrecioVarianteCrearDTO precioDto : vPayload.getPrecios()) {
						if (precioDto != null) {
							precioDto.setVarianteId(vd.getId());
							PrecioVarianteResponseDTO createdPrecio = precioVarianteService.crear(precioDto);
							preciosCreados.add(createdPrecio);
						}
					}
				}
				vr.setPrecios(preciosCreados);

				// 3.3) Inventarios
				List<InventarioVarianteDTO> inventariosCreados = new ArrayList<>();
				if (vPayload.getInventarios() != null) {
					for (InventarioVarianteDTO invDto : vPayload.getInventarios()) {
						if (invDto != null) {
							invDto.setVarianteId(vd.getId());
							InventarioVarianteDTO createdInv = inventarioVarianteService.crear(invDto);
							inventariosCreados.add(createdInv);
						}
					}
				}
				vr.setInventarios(inventariosCreados);

				// 3.4) Physical
				List<PhysicalPropertiesDTO> physicalList = new ArrayList<>();
				if (vPayload.getPhysical() != null && !vPayload.getPhysical().isEmpty()) {
					PhysicalPropertiesDTO physDto = vPayload.getPhysical().get(0);
					if (physDto != null) {
						PhysicalPropertiesDTO createdPhys = varianteFisicoService.crearOActualizarPorVariante(vd.getId(), physDto);
						physicalList.add(createdPhys);
					}
				}
				vr.setPhysical(physicalList);

				// 3.5) Imagenes
				List<ImagenVarianteDTO> imagenesCreadas = new ArrayList<>();
				if (vPayload.getImagenes() != null) {
					for (ImagenVarianteDTO imgDto : vPayload.getImagenes()) {
						if (imgDto != null) {
							imgDto.setVarianteId(vd.getId());
							ImagenVarianteDTO createdImg = imagenVarianteService.crear(imgDto);
							imagenesCreadas.add(createdImg);
						}
					}
				}
				vr.setImagenes(imagenesCreadas);
			} else {
				// sin payload, retornar vacíos
				vr.setVarianteOpciones(new VarianteConOpcionesValoresDTO(vd.getId(), new ArrayList<>()));
				vr.setPrecios(new ArrayList<>());
				vr.setInventarios(new ArrayList<>());
				vr.setPhysical(new ArrayList<>());
				vr.setImagenes(new ArrayList<>());
			}

			variantesResp.add(vr);
		}

		// 4) construir respuesta completa
		ProductoCentralizadoResponseFullDTO out = new ProductoCentralizadoResponseFullDTO();
		out.setProducto(createdProducto);
		out.setVariantes(variantesResp);
		
		log.info("[V2] Producto {} creado con {} variantes y sub-recursos", createdProducto.getId(), variantesResp.size());
		return out;
	}

	/**
	 * Crea las relaciones VarianteOpcion necesarias antes de asignar valores.
	 * Esto evita el error de validación "Alguna opcion no pertenece al producto".
	 */
	private void crearRelacionesVarianteOpcion(Long productoId, ProductoCentralizadoCrearDTO dto, List<VarianteDTO> createdVars) {
		Set<Long> opcionIdsRequeridas = new HashSet<>();
		
		// Recolectar todas las opcionIds del payload
		if (dto.getVariantes() != null) {
			for (VarianteAnidadaCrearDTO vPayload : dto.getVariantes()) {
				if (vPayload != null && vPayload.getVarianteOpciones() != null) {
					for (VarianteOpcionesAsignarDTO asignar : vPayload.getVarianteOpciones()) {
						if (asignar != null && asignar.opciones != null) {
							for (VarianteOpcionesAsignarDTO.OpcionSeleccionada opcionSel : asignar.opciones) {
								if (opcionSel != null && opcionSel.opcionId != null) {
									opcionIdsRequeridas.add(opcionSel.opcionId);
								}
							}
						}
					}
				}
			}
		}

		if (opcionIdsRequeridas.isEmpty()) {
			log.debug("[V2] No hay opciones para crear relaciones");
			return;
		}

		log.debug("[V2] Creando relaciones VarianteOpcion para {} opciones", opcionIdsRequeridas.size());

		// Crear relaciones VarianteOpcion en batch
		List<VarianteOpcion> relacionesNuevas = new ArrayList<>();
		
		// Obtener todas las variantes como entidades para evitar múltiples consultas
		List<Variante> variantesEntities = varianteRepository.findAllById(
			createdVars.stream().map(VarianteDTO::getId).collect(java.util.stream.Collectors.toList())
		);
		
		for (Long opcionId : opcionIdsRequeridas) {
			// Verificar si ya existe la relación a nivel de producto
			boolean exists = varianteOpcionRepository.existsByVariante_Producto_IdAndOpcion_IdAndDeletedAtIsNull(productoId, opcionId);
			
			if (!exists) {
				Opcion opcion = opcionRepository.findById(opcionId)
					.orElseThrow(() -> new IllegalArgumentException("Opcion no encontrada: " + opcionId));
				
				// Crear una relación por cada variante
				for (Variante varianteEntity : variantesEntities) {
					VarianteOpcion vo = new VarianteOpcion();
					vo.setVariante(varianteEntity);
					vo.setOpcion(opcion);
					vo.setRequerido(false);
					vo.setActivo(true);
					vo.setOrden(0);
					relacionesNuevas.add(vo);
				}
			}
		}

		if (!relacionesNuevas.isEmpty()) {
			varianteOpcionRepository.saveAll(relacionesNuevas);
			log.debug("[V2] {} relaciones VarianteOpcion creadas", relacionesNuevas.size());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ProductoCentralizadoResponseFullDTO obtenerPorId(Long productoId) {
		log.debug("[V2] obtenerPorId llamado para productoId={}", productoId);
		
		// Obtener producto
		var producto = productoService.obtenerPorId(productoId);
		
		// Obtener variantes
		List<VarianteDTO> variantes = varianteRepository.findByProducto_Id(productoId)
			.stream()
			.map(VarianteMapper::toDto)
			.collect(java.util.stream.Collectors.toList());
		
		// Construir respuesta completa con sub-recursos por variante
		ProductoCentralizadoResponseFullDTO out = new ProductoCentralizadoResponseFullDTO();
		out.setProducto(producto);
		
		List<VarianteCentralizadaResponseDTO> variantesResp = new ArrayList<>();
		for (VarianteDTO vd : variantes) {
			VarianteCentralizadaResponseDTO vr = new VarianteCentralizadaResponseDTO();
			vr.setId(vd.getId());
			vr.setSku(vd.getSku());
			vr.setAttributesJson(vd.getAttributesJson());
			vr.setAttributesHash(vd.getAttributesHash());
			vr.setEsDefault(vd.getEsDefault());
			vr.setActivo(vd.getActivo());
			
			// Opciones
			try {
				var opciones = varianteOpcionService.obtenerVarianteConOpcionesConValores(vd.getId());
				vr.setVarianteOpciones(opciones != null ? opciones : new VarianteConOpcionesValoresDTO(vd.getId(), new ArrayList<>()));
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo opciones para variante {}: {}", vd.getId(), e.getMessage());
				vr.setVarianteOpciones(new VarianteConOpcionesValoresDTO(vd.getId(), new ArrayList<>()));
			}

			
			// Precios
			try {
				var precios = precioVarianteService.listarPorVarianteId(vd.getId());
				vr.setPrecios(precios != null ? precios : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo precios para variante {}: {}", vd.getId(), e.getMessage());
				vr.setPrecios(new ArrayList<>());
			}
			
			// Inventarios
			try {
				var invDTO = inventarioVarianteService.obtenerPorVarianteId(vd.getId());
				vr.setInventarios(invDTO != null ? List.of(invDTO) : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo inventarios para variante {}: {}", vd.getId(), e.getMessage());
				vr.setInventarios(new ArrayList<>());
			}
			
			// Physical
			try {
				var phys = varianteFisicoService.obtenerPorVarianteId(vd.getId());
				vr.setPhysical(phys != null ? List.of(phys) : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo physical para variante {}: {}", vd.getId(), e.getMessage());
				vr.setPhysical(new ArrayList<>());
			}
			
			// Imagenes
			try {
				var imgs = imagenVarianteService.listarPorVarianteId(vd.getId());
				vr.setImagenes(imgs != null ? imgs : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo imagenes para variante {}: {}", vd.getId(), e.getMessage());
				vr.setImagenes(new ArrayList<>());
			}
			
			variantesResp.add(vr);
		}
		
		out.setVariantes(variantesResp);
		return out;
	}

	
	@Override
	@Transactional(readOnly = true)
	public com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullSimpleDTO obtenerSimplePorId(Long productoId) {
		log.debug("[V2] obtenerSimplePorId llamado para productoId={}", productoId);
		// proteger la obtención del producto contra excepciones que marquen la transacción rollback-only
		ProductoCentralizadoResponseDTO producto = null;
		try {
			producto = productoService.obtenerPorId(productoId);
		} catch (Exception ex) {
			log.error("[V2] Error obteniendo producto {}: {}", productoId, ex.getMessage(), ex);
			return null;
		}
		if (producto == null) return null;

		List<VarianteDTO> variantes;
		try {
			variantes = varianteRepository.findByProducto_Id(productoId)
				.stream()
				.map(VarianteMapper::toDto)
				.collect(java.util.stream.Collectors.toList());
		} catch (Exception ex) {
			log.error("[V2] Error obteniendo variantes para producto {}: {}", productoId, ex.getMessage(), ex);
			return null;
		}

		com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullSimpleDTO out = new com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoCentralizadoResponseFullSimpleDTO();
		out.setProducto(producto);

		List<com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteCentralizadaResponseSimpleDTO> variantesResp = new ArrayList<>();
		for (VarianteDTO vd : variantes) {
			com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteCentralizadaResponseSimpleDTO vr = new com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.VarianteCentralizadaResponseSimpleDTO();
			vr.setId(vd.getId());
			vr.setSku(vd.getSku());
			vr.setAttributesJson(vd.getAttributesJson());
			vr.setAttributesHash(vd.getAttributesHash());
			vr.setEsDefault(vd.getEsDefault());
			vr.setActivo(vd.getActivo());

			try {
				var precios = precioVarianteService.listarPorVarianteId(vd.getId());
				vr.setPrecios(precios != null ? precios : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo precios para variante {}: {}", vd.getId(), e.getMessage(), e);
				vr.setPrecios(new ArrayList<>());
			}

			try {
				var invDTO = inventarioVarianteService.obtenerPorVarianteId(vd.getId());
				vr.setInventarios(invDTO != null ? List.of(invDTO) : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo inventarios para variante {}: {}", vd.getId(), e.getMessage(), e);
				vr.setInventarios(new ArrayList<>());
			}

			try {
				var phys = varianteFisicoService.obtenerPorVarianteId(vd.getId());
				vr.setPhysical(phys != null ? List.of(phys) : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo physical para variante {}: {}", vd.getId(), e.getMessage(), e);
				vr.setPhysical(new ArrayList<>());
			}

			try {
				var imgs = imagenVarianteService.listarPorVarianteId(vd.getId());
				vr.setImagenes(imgs != null ? imgs : new ArrayList<>());
			} catch (Exception e) {
				log.warn("[V2] Error obteniendo imagenes para variante {}: {}", vd.getId(), e.getMessage(), e);
				vr.setImagenes(new ArrayList<>());
			}

			variantesResp.add(vr);
		}

		out.setVariantes(variantesResp);
		return out;
	}

	// helper: calcular sha256 hex
	private static String sha256Hex(String input) {
		if (input == null) return null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) sb.append(String.format("%02x", b & 0xff));
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("Unable to calculate SHA-256", e);
		}
	}

}