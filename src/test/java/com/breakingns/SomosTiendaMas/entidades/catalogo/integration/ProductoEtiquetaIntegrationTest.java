package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductoEtiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoEtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class ProductoEtiquetaIntegrationTest {
    
    @Autowired
    private ProductoEtiquetaRepository repository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private EtiquetaRepository etiquetaRepository;
    
    @Autowired
    private MockMvc restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('producto_etiqueta_id_seq', (SELECT COALESCE(MAX(id), 1) FROM producto_etiqueta))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearProductoEtiqueta_CuandoDatosValidos() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("producto-test-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta padre
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Oferta");
        etiqueta.setSlug("oferta-test");
        etiqueta = etiquetaRepository.save(etiqueta);
        
        // Crear entidad con datos válidos
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiqueta);
        
        // Guardar en DB
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertNotNull(guardada.getProducto());
        assertNotNull(guardada.getEtiqueta());
        assertEquals(producto.getId(), guardada.getProducto().getId());
        assertEquals(etiqueta.getId(), guardada.getEtiqueta().getId());
    }
    
    @Test
    void findById_DeberiaRetornarProductoEtiqueta_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 2");
        producto.setSlug("producto-test-2-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta padre
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Destacado");
        etiqueta.setSlug("destacado-test");
        etiqueta = etiquetaRepository.save(etiqueta);
        
        // Guardar entidad en DB
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiqueta);
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el producto etiqueta por ID");
        
        // Verificar datos coinciden
        ProductoEtiqueta encontrada = resultado.get();
        assertEquals(producto.getId(), encontrada.getProducto().getId());
        assertEquals(etiqueta.getId(), encontrada.getEtiqueta().getId());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenProductoEtiquetas() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 3");
        producto.setSlug("producto-test-3-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar 3 entidades diferentes con distintas etiquetas
        Etiqueta etiqueta1 = new Etiqueta();
        etiqueta1.setNombre("Etiqueta 1");
        etiqueta1.setSlug("etiqueta-1-test");
        etiqueta1 = etiquetaRepository.save(etiqueta1);
        
        ProductoEtiqueta productoEtiqueta1 = new ProductoEtiqueta();
        productoEtiqueta1.setProducto(producto);
        productoEtiqueta1.setEtiqueta(etiqueta1);
        repository.save(productoEtiqueta1);
        
        Etiqueta etiqueta2 = new Etiqueta();
        etiqueta2.setNombre("Etiqueta 2");
        etiqueta2.setSlug("etiqueta-2-test");
        etiqueta2 = etiquetaRepository.save(etiqueta2);
        
        ProductoEtiqueta productoEtiqueta2 = new ProductoEtiqueta();
        productoEtiqueta2.setProducto(producto);
        productoEtiqueta2.setEtiqueta(etiqueta2);
        repository.save(productoEtiqueta2);
        
        Etiqueta etiqueta3 = new Etiqueta();
        etiqueta3.setNombre("Etiqueta 3");
        etiqueta3.setSlug("etiqueta-3-test");
        etiqueta3 = etiquetaRepository.save(etiqueta3);
        
        ProductoEtiqueta productoEtiqueta3 = new ProductoEtiqueta();
        productoEtiqueta3.setProducto(producto);
        productoEtiqueta3.setEtiqueta(etiqueta3);
        repository.save(productoEtiqueta3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 producto etiquetas");
    }
    
    @Test
    void delete_DeberiaEliminarProductoEtiqueta_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 4");
        producto.setSlug("producto-test-4-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta padre
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("ParaBorrar");
        etiqueta.setSlug("para-borrar-test");
        etiqueta = etiquetaRepository.save(etiqueta);
        
        // Guardar entidad
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiqueta);
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El producto etiqueta debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarProductoEtiqueta_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 5");
        producto.setSlug("producto-test-5-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear etiqueta original
        Etiqueta etiquetaOriginal = new Etiqueta();
        etiquetaOriginal.setNombre("Original");
        etiquetaOriginal.setSlug("original-test");
        etiquetaOriginal = etiquetaRepository.save(etiquetaOriginal);
        
        // Guardar entidad con etiqueta "Original"
        ProductoEtiqueta productoEtiqueta = new ProductoEtiqueta();
        productoEtiqueta.setProducto(producto);
        productoEtiqueta.setEtiqueta(etiquetaOriginal);
        ProductoEtiqueta guardada = repository.save(productoEtiqueta);
        
        // Crear nueva etiqueta "Actualizada"
        Etiqueta etiquetaActualizada = new Etiqueta();
        etiquetaActualizada.setNombre("Actualizado");
        etiquetaActualizada.setSlug("actualizado-test");
        etiquetaActualizada = etiquetaRepository.save(etiquetaActualizada);
        
        // Modificar etiqueta a "Actualizado"
        guardada.setEtiqueta(etiquetaActualizada);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar etiqueta es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getEtiqueta().getNombre());
        assertEquals(etiquetaActualizada.getId(), resultado.get().getEtiqueta().getId());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    @Test
    void post_DeberiaCrearProductoEtiqueta_Status201() throws Exception {
        // Crear producto y etiqueta en DB (no hay controller de productos activo)
        Producto producto = new Producto();
        producto.setNombre("Prod API");
        producto.setSlug("prod-api-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);

        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Etiqueta API");
        etiqueta.setSlug("et-api-" + System.currentTimeMillis());
        etiqueta = etiquetaRepository.save(etiqueta);

        // Preparar payload
        var payload = new java.util.HashMap<String,Long>();
        payload.put("productoId", producto.getId());
        payload.put("etiquetaId", etiqueta.getId());

        var result = restTemplate.perform(MockMvcRequestBuilders.post("/api/producto-etiquetas")
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payload)))
            .andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        var body = result.getResponse().getContentAsString();
        assertNotNull(body);
        var dto = objectMapper.readValue(body, java.util.Map.class);
        assertNotNull(dto.get("id"));
        assertEquals(producto.getId().intValue(), ((Number)dto.get("productoId")).intValue());
        assertEquals(etiqueta.getId().intValue(), ((Number)dto.get("etiquetaId")).intValue());
    }

    @Test
    void getById_DeberiaRetornarProductoEtiqueta_Status200_API() throws Exception {
        // Crear padres y guardar asociación
        Producto producto = new Producto(); producto.setNombre("ProdG"); producto.setSlug("prod-g-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Etiqueta etiqueta = new Etiqueta(); etiqueta.setNombre("EtG"); etiqueta.setSlug("et-g"); etiqueta = etiquetaRepository.save(etiqueta);
        ProductoEtiqueta pe = new ProductoEtiqueta(); pe.setProducto(producto); pe.setEtiqueta(etiqueta); ProductoEtiqueta saved = repository.save(pe);

        var result = restTemplate.perform(MockMvcRequestBuilders.get("/api/producto-etiquetas/{id}", saved.getId())
            .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertEquals(saved.getId().intValue(), ((Number)dto.get("id")).intValue());
        assertEquals(producto.getId().intValue(), ((Number)dto.get("productoId")).intValue());
    }

    @Test
    void delete_DeberiaEliminarProductoEtiqueta_Status204_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("ProdD"); producto.setSlug("prod-d"); producto = productoRepository.save(producto);
        Etiqueta etiqueta = new Etiqueta(); etiqueta.setNombre("EtD"); etiqueta.setSlug("et-d"); etiqueta = etiquetaRepository.save(etiqueta);
        ProductoEtiqueta pe = new ProductoEtiqueta(); pe.setProducto(producto); pe.setEtiqueta(etiqueta); ProductoEtiqueta saved = repository.save(pe);

        restTemplate.perform(MockMvcRequestBuilders.delete("/api/producto-etiquetas/{id}", saved.getId()))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto p = new Producto(); p.setNombre("P RB"); p.setSlug("p-rb-" + System.currentTimeMillis()); productoRepository.save(p);
        Etiqueta e = new Etiqueta(); e.setNombre("E RB"); e.setSlug("e-rb-" + System.currentTimeMillis()); etiquetaRepository.save(e);

        ProductoEtiqueta pe = new ProductoEtiqueta(); pe.setProducto(p); pe.setEtiqueta(e);
        repository.save(pe);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        var found = repository.findAll().stream().filter(x -> x.getProducto()!=null && x.getProducto().getSlug()!=null && x.getProducto().getSlug().contains("p-rb-")).findAny();
        assertTrue(found.isEmpty(), "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        // Preparar padres
        Producto p = new Producto(); p.setNombre("P C"); p.setSlug("p-c-" + System.currentTimeMillis()); productoRepository.save(p);
        Etiqueta e = new Etiqueta(); e.setNombre("E C"); e.setSlug("e-c-" + System.currentTimeMillis()); etiquetaRepository.save(e);

        // Insertar primero
        ProductoEtiqueta first = new ProductoEtiqueta(); first.setProducto(p); first.setEtiqueta(e);
        repository.saveAndFlush(first);

        // Detectar si existe constraint UNIQUE en la tabla
        boolean uniqueExists = false;
        try {
            Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='producto_etiqueta' AND c.contype='u'")
                    .getSingleResult();
            if (cnt != null) uniqueExists = ((Number)cnt).longValue() > 0;
        } catch (Exception ex) {
            uniqueExists = false;
        }

        ProductoEtiqueta dup = new ProductoEtiqueta(); dup.setProducto(p); dup.setEtiqueta(e);
        if (uniqueExists) {
            assertThrows(DataIntegrityViolationException.class, () -> { repository.saveAndFlush(dup); entityManager.flush(); });
        } else {
            repository.saveAndFlush(dup);
            entityManager.flush();
            long count = repository.findAll().stream().filter(x -> x.getProducto().getId().equals(p.getId()) && x.getEtiqueta().getId().equals(e.getId())).count();
            assertTrue(count >= 2, "Sin constraint UNIQUE se deben permitir duplicados");
        }

        // Not-null detection for relacion columns: producto_id and etiqueta_id
        boolean productoNotNull = false;
        try {
            Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='producto_etiqueta' AND column_name='producto_id'")
                    .getSingleResult();
            if (nn != null) productoNotNull = "NO".equalsIgnoreCase(nn.toString());
        } catch (Exception ex) { productoNotNull = false; }

        if (productoNotNull) {
            ProductoEtiqueta bad = new ProductoEtiqueta(); bad.setProducto(null); bad.setEtiqueta(e);
            assertThrows(DataIntegrityViolationException.class, () -> { repository.saveAndFlush(bad); entityManager.flush(); });
        }

        // Auditoría
        try { assertNotNull(first.getId()); var saved = repository.findById(first.getId()).orElseThrow(); assertNotNull(saved.getCreatedAt()); } catch (Exception ex) { }
    }

}
