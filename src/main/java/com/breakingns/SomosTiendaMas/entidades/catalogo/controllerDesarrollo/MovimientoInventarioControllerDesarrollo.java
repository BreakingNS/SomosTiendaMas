package com.breakingns.SomosTiendaMas.entidades.catalogo.controllerDesarrollo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoFiltroDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.movimiento.MovimientoResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IMovimientoInventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dev/api/movimientos")
public class MovimientoInventarioControllerDesarrollo {

	private final IMovimientoInventarioService service;

	public MovimientoInventarioControllerDesarrollo(IMovimientoInventarioService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<MovimientoResumenDTO>> listarTodos() {
		MovimientoFiltroDTO filtro = new MovimientoFiltroDTO();
		List<MovimientoResumenDTO> list = service.filtrar(filtro);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{id}")
	public ResponseEntity<MovimientoResponseDTO> obtenerPorId(@PathVariable Long id) {
		MovimientoResponseDTO dto = service.obtenerPorId(id);
		return ResponseEntity.ok(dto);
	}

}
