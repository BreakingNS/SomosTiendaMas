package com.breakingns.SomosTiendaMas.auth.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import com.breakingns.SomosTiendaMas.auth.dto.shared.DepartamentoDTO;
import com.breakingns.SomosTiendaMas.auth.dto.shared.LocalidadDTO;
import com.breakingns.SomosTiendaMas.auth.dto.shared.MunicipioDTO;
import com.breakingns.SomosTiendaMas.auth.dto.shared.ProvinciaDTO;
import com.breakingns.SomosTiendaMas.auth.service.ImportDepartamentosService;
import com.breakingns.SomosTiendaMas.auth.service.ImportLocalidadesService;
import com.breakingns.SomosTiendaMas.auth.service.ImportMunicipiosService;
import com.breakingns.SomosTiendaMas.auth.service.ImportProvinciasService;
import com.breakingns.SomosTiendaMas.auth.service.UbicacionesService;

@RestController
@RequestMapping("/api/import-ubicaciones")
public class ImportUbicacionesController {

    @Autowired
    private ImportProvinciasService importProvinciasService;
    @Autowired
    private ImportDepartamentosService importDepartamentosService;
    @Autowired
    private ImportMunicipiosService importMunicipiosService;
    @Autowired
    private ImportLocalidadesService importLocalidadesService;

    @Autowired
    private UbicacionesService ubicacionesService;

    @PostMapping("/provincias")
    public String importarProvincias() {
        try {
            importProvinciasService.importarProvincias();
            return "Provincias importadas correctamente";
        } catch (Exception e) {
            return "Error al importar provincias: " + e.getMessage();
        }
    }

    @PostMapping("/departamentos")
    public String importarDepartamentos() {
        try {
            importDepartamentosService.importarDepartamentos();
            return "Departamentos importados correctamente";
        } catch (Exception e) {
            return "Error al importar departamentos: " + e.getMessage();
        }
    }

    @PostMapping("/localidades")
    public String importarLocalidades() {
        try {
            importLocalidadesService.importarLocalidades();
            return "Localidades importadas correctamente";
        } catch (Exception e) {
            return "Error al importar localidades: " + e.getMessage();
        }
    }

    @PostMapping("/municipios")
    public String importarMunicipios() {
        try {
            importMunicipiosService.importarMunicipios();
            return "Municipios importados correctamente";
        } catch (Exception e) {
            return "Error al importar municipios: " + e.getMessage();
        }
    }

    // Obtener todas las provincias
    @GetMapping("/provincias")
    public List<ProvinciaDTO> getProvincias() {
        return ubicacionesService.getProvinciasDTO();
    }

    @GetMapping("/departamentos")
    public List<DepartamentoDTO> getDepartamentos(@RequestParam Long provinciaId) {
        return ubicacionesService.getDepartamentosPorProvinciaDTO(provinciaId);
    }

    @GetMapping("/municipios")
    public List<MunicipioDTO> getMunicipios(@RequestParam Long departamentoId) {
        return ubicacionesService.getMunicipiosPorDepartamentoDTO(departamentoId);
    }

    @GetMapping("/localidades")
    public List<LocalidadDTO> getLocalidades(@RequestParam Long municipioId) {
        return ubicacionesService.getLocalidadesPorMunicipioDTO(municipioId);
    }
}