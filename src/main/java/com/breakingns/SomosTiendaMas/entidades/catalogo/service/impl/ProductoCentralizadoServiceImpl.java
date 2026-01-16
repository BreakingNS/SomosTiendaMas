package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductCreateDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.ProductoDetalleResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado.PhysicalPropertiesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoConOpcionesDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_opcion.ProductoOpcionesAsignarDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1.VarianteListaDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoCentralizadoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoOpcionService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductoService;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IVarianteFisicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ProductoCentralizadoServiceImpl implements IProductoCentralizadoService {

	private final IProductoService productoService;
	private final IProductoOpcionService productoOpcionService;
	private final IVarianteFisicoService varianteFisicoService;

	public ProductoCentralizadoServiceImpl(IProductoService productoService,
										   IProductoOpcionService productoOpcionService,
										   IVarianteFisicoService varianteFisicoService) {
		this.productoService = productoService;
		this.productoOpcionService = productoOpcionService;
		this.varianteFisicoService = varianteFisicoService;
	}

	@Override
	public ProductoDetalleResponseDTO crear(ProductCreateDTO dto) {
		if (dto == null) throw new IllegalArgumentException("payload required");

		ProductoCrearDTO pDto = dto.getProducto();
		if (pDto == null) throw new IllegalArgumentException("producto required");

		// crear producto (ProductoService.crear maneja varianteDefault si viene en producto DTO)
		ProductoResponseDTO created = productoService.crear(pDto);

		// asignar opciones de producto si vienen
		ProductoOpcionesAsignarDTO opciones = dto.getOpciones();
		if (opciones != null) {
			// asegurar que venga el productoId
			if (opciones.productoId == null) opciones.productoId = created.getId();
			// usuario: usar 'system' por ahora
			productoOpcionService.asignarOpciones(opciones, "system");
		}

		// si se envía physical en payload, guardarlo en la variante default creada
		PhysicalPropertiesDTO physical = dto.getPhysical();
		if (physical != null) {
			// intentar localizar variante default en la respuesta del producto
			Long varianteId = findDefaultVarianteId(created);
			if (varianteId == null) throw new NoSuchElementException("No default variant found for product " + created.getId());
			varianteFisicoService.crearOActualizarPorVariante(varianteId, physical);
		}

		// construir respuesta: usar productoService.obtenerPorId para enriquecer
		ProductoResponseDTO prodFull = productoService.obtenerPorId(created.getId());
		ProductoDetalleResponseDTO out = new ProductoDetalleResponseDTO();
		out.setProducto(prodFull);

		// poblar resumen de opciones (sin valores) si existen
		try {
			ProductoConOpcionesDTO po = productoOpcionService.obtenerProductoConOpciones(created.getId());
			if (po != null) out.setOpciones(po.getOpciones());
		} catch (Exception ignored) {}

		// imagenes/precio/stock se manejan a nivel de variante; el agregador puede resolverlos cuando se solicite
		return out;
	}

	private Long findDefaultVarianteId(ProductoResponseDTO created) {
		if (created == null) return null;
		List<VarianteListaDTO> list = created.getVariantes();
		if (list == null || list.isEmpty()) return null;
		for (VarianteListaDTO v : list) {
			if (Boolean.TRUE.equals(v.getEsDefault())) return v.getId();
		}
		// fallback: devolver la primera
		return list.get(0).getId();
	}
}

