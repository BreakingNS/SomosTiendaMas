package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class AEtiquetaPruebaIntegrationTest {

    private static String SHARED_ROLLBACK_SLUG = "rollback-slug-shared";
    
    @Autowired
    private EtiquetaRepository repository;
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearEtiqueta_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Oferta");
        etiqueta.setSlug("oferta-test");
        
        // Guardar en DB
        Etiqueta guardada = repository.save(etiqueta);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Oferta", guardada.getNombre());
        assertEquals("oferta-test", guardada.getSlug());
    }

    // === TESTS DE API (Controller) ===
    // Tests de integración a nivel HTTP usando `restTemplate`.

    @Test
    void post_DeberiaCrearEtiqueta_Status201() {
        Etiqueta payload = new Etiqueta();
        payload.setNombre("Etiqueta API");
        payload.setSlug("etiqueta-api-" + System.currentTimeMillis());
        try {
            int before = repository.findAll().size();
            var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .post("/api/etiquetas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            int after = repository.findAll().size();
            assertEquals(before + 1, after, "Debe haberse creado una nueva etiqueta en la BD");
        } catch (Exception e) {
            fail("Error en MockMvc POST: " + e.getMessage());
        }
    }

    // === VERIFICACIONES DB REALES ===

    @Test
    @Order(1)
    void tx_InsertaYSeRollbackea() {
        String slug = SHARED_ROLLBACK_SLUG + System.currentTimeMillis();
        Etiqueta e = new Etiqueta();
        e.setNombre("Temp");
        e.setSlug(slug);
        repository.save(e);

        // visible dentro de la transacción del test
        assertTrue(repository.findBySlug(slug).isPresent());

        // guardar el slug para el siguiente test
        SHARED_ROLLBACK_SLUG = slug;
    }

    @Test
    @Order(2)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void verify_NoPersistioDespuesDeRollback() {
        // El test anterior es transaccional y Spring lo rollbackeará al finalizar,
        // por eso aquí, ejecutando fuera de la transacción, no debería existir.
        assertTrue(repository.findBySlug(SHARED_ROLLBACK_SLUG).isEmpty());
    }

    @Test
    void unique_Slug_DeberiaLanzarExcepcion() {
        String slug = "dup-slug-" + System.currentTimeMillis();
        Etiqueta a = new Etiqueta(); a.setNombre("A"); a.setSlug(slug);
        Etiqueta b = new Etiqueta(); b.setNombre("B"); b.setSlug(slug);
        repository.saveAndFlush(a);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(b));
    }

    @Test
    void notNull_Nombre_DeberiaLanzarExcepcion() {
        Etiqueta e = new Etiqueta();
        e.setSlug("no-name-" + System.currentTimeMillis());
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(e));
    }

    @Test
    void auditoria_CreatedAndUpdated_ShouldWork() throws InterruptedException {
        Etiqueta e = new Etiqueta();
        e.setNombre("Audit");
        e.setSlug("audit-" + System.currentTimeMillis());
        Etiqueta saved = repository.saveAndFlush(e);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        var created = saved.getCreatedAt();
        Thread.sleep(50);
        saved.setNombre("Audit-Updated");
        Etiqueta updated = repository.saveAndFlush(saved);

        assertTrue(updated.getUpdatedAt().isAfter(created));
    }

}
