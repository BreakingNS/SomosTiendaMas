package com.breakingns.SomosTiendaMas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakingns.SomosTiendaMas.auth.service.ImportProvinciasService;
import com.breakingns.SomosTiendaMas.auth.service.ImportDepartamentosService;
import com.breakingns.SomosTiendaMas.auth.service.ImportMunicipiosService;
import com.breakingns.SomosTiendaMas.auth.service.ImportLocalidadesService;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.ImportCodigosAreaService;

@Configuration
public class DataInitRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitRunner.class);

    @Autowired
    private ImportProvinciasService importProvinciasService;
    @Autowired
    private ImportDepartamentosService importDepartamentosService;
    @Autowired
    private ImportMunicipiosService importMunicipiosService;
    @Autowired
    private ImportLocalidadesService importLocalidadesService;
    @Autowired
    private ImportCodigosAreaService importCodigosAreaService;

    @Bean
    public ApplicationRunner dataInitializer() {
        return args -> {
            try {
                if (importProvinciasService.count() == 0) {
                    log.info("Tabla provincias vacía: importando desde Excel...");
                    importProvinciasService.importarProvincias();
                    log.info("Importación provincias finalizada.");
                } else {
                    log.info("Tabla provincias ya poblada ({} registros)", importProvinciasService.count());
                }

                if (importDepartamentosService.count() == 0) {
                    log.info("Tabla departamentos vacía: importando desde Excel...");
                    importDepartamentosService.importarDepartamentos();
                    log.info("Importación departamentos finalizada.");
                } else {
                    log.info("Tabla departamentos ya poblada ({} registros)", importDepartamentosService.count());
                }

                if (importMunicipiosService.count() == 0) {
                    log.info("Tabla municipios vacía: importando desde Excel...");
                    importMunicipiosService.importarMunicipios();
                    log.info("Importación municipios finalizada.");
                } else {
                    log.info("Tabla municipios ya poblada ({} registros)", importMunicipiosService.count());
                }

                if (importLocalidadesService.count() == 0) {
                    log.info("Tabla localidades vacía: importando desde Excel...");
                    importLocalidadesService.importarLocalidades();
                    log.info("Importación localidades finalizada.");
                } else {
                    log.info("Tabla localidades ya poblada ({} registros)", importLocalidadesService.count());
                }

                if (importCodigosAreaService.count() == 0) {
                    log.info("Tabla códigos de área vacía: importando desde Excel...");
                    importCodigosAreaService.importarCodigosArea();
                    log.info("Importación códigos de área finalizada.");
                } else {
                    log.info("Tabla códigos de área ya poblada ({} registros)", importCodigosAreaService.count());
                }

            } catch (Exception e) {
                log.error("Error durante la inicialización de datos: {}", e.getMessage(), e);
            }
        };
    }
}
