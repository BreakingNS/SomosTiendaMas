package com.breakingns.SomosTiendaMas.auth.config;

import com.breakingns.SomosTiendaMas.auth.model.Rol;
import com.breakingns.SomosTiendaMas.auth.model.RolNombre;
import com.breakingns.SomosTiendaMas.auth.repository.IRolRepository;
import com.breakingns.SomosTiendaMas.auth.service.ImportDepartamentosService;
import com.breakingns.SomosTiendaMas.auth.service.ImportLocalidadesService;
import com.breakingns.SomosTiendaMas.auth.service.ImportMunicipiosService;
import com.breakingns.SomosTiendaMas.auth.service.ImportProvinciasService;
import com.breakingns.SomosTiendaMas.entidades.telefono.service.ImportCodigosAreaService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataLoader implements CommandLineRunner {

    private final IRolRepository rolRepository;
    private final ImportProvinciasService importProvinciasService;
    private final ImportDepartamentosService importDepartamentosService;
    private final ImportMunicipiosService importMunicipiosService;
    private final ImportLocalidadesService importLocalidadesService;
    private final ImportCodigosAreaService importCodigosAreaService;

    public DataLoader(
        IRolRepository rolRepository,
        ImportProvinciasService importProvinciasService,
        ImportDepartamentosService importDepartamentosService,
        ImportMunicipiosService importMunicipiosService,
        ImportLocalidadesService importLocalidadesService,
        ImportCodigosAreaService importCodigosAreaService
    ) {
        this.rolRepository = rolRepository;
        this.importProvinciasService = importProvinciasService;
        this.importDepartamentosService = importDepartamentosService;
        this.importMunicipiosService = importMunicipiosService;
        this.importLocalidadesService = importLocalidadesService;
        this.importCodigosAreaService = importCodigosAreaService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Cargar roles si no existen
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol(RolNombre.ROLE_SUPERADMIN));
            rolRepository.save(new Rol(RolNombre.ROLE_ADMIN));
            rolRepository.save(new Rol(RolNombre.ROLE_EMPRESA));
            rolRepository.save(new Rol(RolNombre.ROLE_MODERADOR));
            rolRepository.save(new Rol(RolNombre.ROLE_SOPORTE));
            rolRepository.save(new Rol(RolNombre.ROLE_ANALISTA));
            rolRepository.save(new Rol(RolNombre.ROLE_USUARIO));
            System.out.println("Roles cargados correctamente.");
        } else {
            System.out.println("Los roles ya existen.");
        }

        // Importar provincias si la tabla está vacía
        if (importProvinciasService.count() == 0) {
            importProvinciasService.importarProvincias();
            System.out.println("Provincias importadas correctamente.");
        }
         
        // Importar departamentos si la tabla está vacía
        if (importDepartamentosService.count() == 0) {
            importDepartamentosService.importarDepartamentos();
            System.out.println("Departamentos importados correctamente.");
        }
        
        // Importar municipios si la tabla está vacía
        if (importMunicipiosService.count() == 0) {
            importMunicipiosService.importarMunicipios();
            System.out.println("Municipios importados correctamente.");
        }
        
        // Importar localidades si la tabla está vacía
        if (importLocalidadesService.count() == 0) {
            importLocalidadesService.importarLocalidades();
            System.out.println("Localidades importadas correctamente.");
        }
        
        // Importar códigos de área si la tabla está vacía
        if (importCodigosAreaService.count() == 0) {
            importCodigosAreaService.importarCodigosArea();
            System.out.println("Códigos de área importados correctamente.");
        }
    }
}