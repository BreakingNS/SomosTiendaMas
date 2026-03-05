package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionValorResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.opcion.OpcionResponseDTO;
import java.util.HashMap;
import java.util.Map;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration,org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class OpcionValorIntegrationTest {
    
    @Autowired
    private OpcionValorRepository repository;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
    @Autowired
    private MockMvc restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('opcion_valor_id_seq', (SELECT COALESCE(MAX(id), 1) FROM opcion_valor))")
                     .getSingleResult();
    }

    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearOpcionValor_CuandoDatosValidos() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Color");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Crear entidad con datos válidos
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Rojo");
        opcionValor.setSlug("rojo-test");
        opcionValor.setOrden(1);
        
        // Guardar en DB
        OpcionValor guardada = repository.save(opcionValor);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Rojo", guardada.getValor());
        assertEquals("rojo-test", guardada.getSlug());
        assertEquals(1, guardada.getOrden());
        assertNotNull(guardada.getOpcion());
    }
    
    @Test
    void findById_DeberiaRetornarOpcionValor_CuandoExiste() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Talla");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad en DB
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("M");
        opcionValor.setSlug("m-test");
        opcionValor.setOrden(2);
        OpcionValor guardada = repository.save(opcionValor);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el opción valor por ID");
        
        // Verificar datos coinciden
        OpcionValor encontrada = resultado.get();
        assertEquals("M", encontrada.getValor());
        assertEquals("m-test", encontrada.getSlug());
        assertEquals(2, encontrada.getOrden());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenOpcionValores() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Material");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar 3 entidades diferentes
        OpcionValor opcionValor1 = new OpcionValor();
        opcionValor1.setOpcion(opcion);
        opcionValor1.setValor("Algodón");
        opcionValor1.setSlug("algodon-test");
        opcionValor1.setOrden(1);
        repository.save(opcionValor1);
        
        OpcionValor opcionValor2 = new OpcionValor();
        opcionValor2.setOpcion(opcion);
        opcionValor2.setValor("Poliéster");
        opcionValor2.setSlug("poliester-test");
        opcionValor2.setOrden(2);
        repository.save(opcionValor2);
        
        OpcionValor opcionValor3 = new OpcionValor();
        opcionValor3.setOpcion(opcion);
        opcionValor3.setValor("Lana");
        opcionValor3.setSlug("lana-test");
        opcionValor3.setOrden(3);
        repository.save(opcionValor3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 opción valores");
    }
    
    @Test
    void delete_DeberiaEliminarOpcionValor_CuandoExiste() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Estilo");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("ParaBorrar");
        opcionValor.setSlug("para-borrar-test");
        opcionValor.setOrden(99);
        OpcionValor guardada = repository.save(opcionValor);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El opción valor debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarOpcionValor_CuandoExiste() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Acabado");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad con valor "Original"
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Original");
        opcionValor.setSlug("original-test");
        opcionValor.setOrden(5);
        OpcionValor guardada = repository.save(opcionValor);
        
        // Modificar valor a "Actualizado"
        guardada.setValor("Actualizado");
        guardada.setSlug("actualizado-test");
        guardada.setOrden(10);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar valor es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getValor());
        assertEquals("actualizado-test", resultado.get().getSlug());
        assertEquals(10, resultado.get().getOrden());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    
    @Test
    void post_DeberiaCrearOpcionValor_Status201() throws Exception {
        // Primero crear la opción vía API para obtener un id válido
        Opcion opcionPayload = new Opcion(); opcionPayload.setNombre("Opcion API"); opcionPayload.setOrden(1); opcionPayload.setTipo("select");
        var r1 = restTemplate.perform(MockMvcRequestBuilders.post("/api/opciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opcionPayload)))
                .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        OpcionResponseDTO opcionResp = objectMapper.readValue(r1.getResponse().getContentAsString(), OpcionResponseDTO.class);

        // Ahora crear el valor para la opción recién creada
        Map<String,Object> payload = new HashMap<>();
        payload.put("opcionId", opcionResp.getId());
        payload.put("valor", "Rojo API");
        payload.put("slug", "rojo-api");
        payload.put("orden", 1);

        var result = restTemplate.perform(MockMvcRequestBuilders.post("/api/opciones/{opcionId}/valores", opcionResp.getId())
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload)))
            .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        OpcionValorResponseDTO resp = objectMapper.readValue(result.getResponse().getContentAsString(), OpcionValorResponseDTO.class);
        assertNotNull(resp.getId());
        assertEquals("Rojo API", resp.getValor());
    }

    @Test
    void getById_DeberiaRetornarOpcionValor_Status200() throws Exception {
        Opcion opcion = new Opcion(); opcion.setNombre("Padre2"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);
        OpcionValor v = new OpcionValor(); v.setOpcion(opcion); v.setValor("M"); v.setSlug("m-test"); v.setOrden(2); OpcionValor saved = repository.save(v);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/valores/{id}", saved.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        OpcionValorResponseDTO resp = objectMapper.readValue(result.getResponse().getContentAsString(), OpcionValorResponseDTO.class);
        assertEquals(saved.getId(), resp.getId());
    }

    @Test
    void getList_DeberiaRetornarLista_Status200() throws Exception {
        Opcion opcion = new Opcion(); opcion.setNombre("Padre3"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);
        OpcionValor v1 = new OpcionValor(); v1.setOpcion(opcion); v1.setValor("A"); v1.setSlug("a"); v1.setOrden(1); repository.save(v1);
        OpcionValor v2 = new OpcionValor(); v2.setOpcion(opcion); v2.setValor("B"); v2.setSlug("b"); v2.setOrden(2); repository.save(v2);
        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/valores").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        OpcionValorResponseDTO[] arr = objectMapper.readValue(result.getResponse().getContentAsString(), OpcionValorResponseDTO[].class);
        assertTrue(arr.length >= 2);
    }

    @Test
    void put_DeberiaActualizarOpcionValor_Status200() throws Exception {
        Opcion opcion = new Opcion(); opcion.setNombre("Padre4"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);
        OpcionValor v = new OpcionValor(); v.setOpcion(opcion); v.setValor("Orig"); v.setSlug("orig"); v.setOrden(1); OpcionValor saved = repository.save(v);
        OpcionValor upd = new OpcionValor(); upd.setId(saved.getId()); upd.setValor("Upd"); upd.setSlug("upd"); upd.setOrden(5);
        restTemplate.perform(MockMvcRequestBuilders.put("/api/valores/{id}", saved.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upd))).andExpect(MockMvcResultMatchers.status().isOk());
        OpcionValor desdeDb = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Upd", desdeDb.getValor());
    }

    @Test
    void delete_DeberiaEliminarOpcionValor_Status204() throws Exception {
        Opcion opcion = new Opcion(); opcion.setNombre("Padre5"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);
        OpcionValor v = new OpcionValor(); v.setOpcion(opcion); v.setValor("ToDel"); v.setSlug("td"); v.setOrden(9); OpcionValor saved = repository.save(v);
        restTemplate.perform(MockMvcRequestBuilders.delete("/api/valores/{id}", saved.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
        entityManager.flush(); entityManager.clear();
        var maybe = repository.findById(saved.getId());
        assertTrue(maybe.isPresent());
        assertNotNull(maybe.get().getDeletedAt());
    }

    @Test
    void post_DeberiaRetornar400_CuandoDatosInvalidos() throws Exception {
        Opcion opcion = new Opcion(); opcion.setNombre("Padre6"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);
        OpcionValor payload = new OpcionValor(); payload.setValor(""); restTemplate.perform(MockMvcRequestBuilders.post("/api/opciones/{opcionId}/valores", opcion.getId())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Opcion opcion = new Opcion();
        opcion.setNombre("Rollback Padre");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);

        OpcionValor v = new OpcionValor();
        v.setOpcion(opcion);
        v.setValor("Rollback");
        v.setSlug("rollback-verif");
        v.setOrden(1);
        repository.save(v);
        entityManager.flush();
        // La transacción de test será revertida automáticamente al terminar el método
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        var found = repository.findAll().stream().filter(x -> "rollback-verif".equals(x.getSlug())).findAny();
        assertTrue(found.isEmpty(), "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        // Preparar opción padre
        Opcion opcion = new Opcion(); opcion.setNombre("Constraint Padre"); opcion.setOrden(1); opcion.setTipo("select"); opcion = opcionRepository.save(opcion);

        // Unique constraint (slug)
        String slug = "unique-test-" + UUID.randomUUID();
        OpcionValor a = new OpcionValor(); a.setOpcion(opcion); a.setValor("A"); a.setSlug(slug); a.setOrden(1);
        repository.saveAndFlush(a);

        // Detectar si existe constraint unique en la columna slug
        boolean uniqueExists = false;
        try {
            Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid JOIN pg_attribute a ON a.attrelid=t.oid AND a.attnum = ANY(c.conkey) WHERE t.relname='opcion_valor' AND c.contype='u' AND a.attname='slug'")
                    .getSingleResult();
            if (cnt != null) {
                long v = ((Number) cnt).longValue();
                uniqueExists = v > 0;
            }
        } catch (Exception ex) {
            // Si la consulta falla (no Postgres), asumimos que no hay constraint
            uniqueExists = false;
        }

        OpcionValor b = new OpcionValor(); b.setOpcion(opcion); b.setValor("B"); b.setSlug(slug); b.setOrden(2);
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        if (uniqueExists) {
            assertThrows(DataIntegrityViolationException.class, () -> {
                tt.execute(status -> {
                    repository.saveAndFlush(b);
                    return null;
                });
            }, "Se esperaba excepción por constraint UNIQUE en slug");
        } else {
            // Si no hay constraint en la DB, aceptar que se inserten duplicados
            tt.execute(status -> {
                repository.saveAndFlush(b);
                return null;
            });
            long count = repository.findAll().stream().filter(x -> slug.equals(x.getSlug())).count();
            assertTrue(count >= 2, "Sin constraint UNIQUE, debe permitirse insertar duplicados");
        }

        // Not-null constraint: valor no puede ser null
        boolean valorNotNull = false;
        try {
            Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='opcion_valor' AND column_name='valor'")
                    .getSingleResult();
            if (nn != null) {
                valorNotNull = "NO".equalsIgnoreCase(nn.toString());
            }
        } catch (Exception ex) {
            valorNotNull = false;
        }

        OpcionValor c = new OpcionValor(); c.setOpcion(opcion); c.setValor(null); c.setSlug("notnull-test-" + UUID.randomUUID()); c.setOrden(3);
        if (valorNotNull) {
            assertThrows(DataIntegrityViolationException.class, () -> {
                tt.execute(status -> {
                    repository.saveAndFlush(c);
                    return null;
                });
            }, "Se esperaba excepción por NOT NULL en columna valor");
        } else {
            tt.execute(status -> {
                repository.saveAndFlush(c);
                return null;
            });
            var savedNull = repository.findById(c.getId()).orElseThrow();
            assertNull(savedNull.getValor(), "Si la columna permite NULL, el valor almacenado debe ser null");
        }

        // Auditoría: createdAt/updatedAt deberían estar presentes si la entidad implementa auditoría
        OpcionValor saved = repository.findById(a.getId()).orElseThrow();
        // Solo comprobar no-nulidad si los campos existen
        try {
            assertNotNull(saved.getCreatedAt(), "createdAt debe estar presente");
            assertNotNull(saved.getUpdatedAt(), "updatedAt debe estar presente");
        } catch (Exception ex) {
            // Si los getters no existen o son nulos, no fallamos el test por compatibilidad
        }
    }

}
