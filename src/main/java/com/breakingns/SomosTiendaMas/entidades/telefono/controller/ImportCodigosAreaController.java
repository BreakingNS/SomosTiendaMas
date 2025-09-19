package com.breakingns.SomosTiendaMas.entidades.telefono.controller;

import com.breakingns.SomosTiendaMas.entidades.telefono.dto.CodigoAreaDTO;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.CodigosAreaService;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.ImportCodigosAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/import-codigos-area")
public class ImportCodigosAreaController {

    @Autowired
    private ImportCodigosAreaService importCodigosAreaService;

    @Autowired
    private CodigosAreaService codigosAreaService;

    @PostMapping("/excel")
    public String importarCodigosArea() {
        try {
            importCodigosAreaService.importarCodigosArea();
            return "Códigos de área importados";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // GET para listar todos los códigos
    @GetMapping
    public ResponseEntity<List<CodigoAreaDTO>> listarTodos() {
        List<CodigoAreaDTO> lista = codigosAreaService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    // GET para buscar por código
    @GetMapping("/buscar")
    public ResponseEntity<CodigoAreaDTO> buscarPorCodigo(@RequestParam("codigo") String codigo) {
        return codigosAreaService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}