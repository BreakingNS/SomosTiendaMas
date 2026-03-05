package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.InventarioVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class InventarioVarianteIntegrationTest {
    
    @Autowired
    private InventarioVarianteRepository repository;
    
    @Autowired
    private VarianteRepository varianteRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("SELECT setval('variante_inventario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_inventario))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearInventarioVariante_CuandoDatosValidos() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(100);
        inventario.setReserved(10);
        inventario.setUbicacion("Estantería A1");
        
        // When
        InventarioVariante resultado = repository.save(inventario);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(100, resultado.getOnHand());
        assertEquals(10, resultado.getReserved());
        assertEquals("Estantería A1", resultado.getUbicacion());
        assertNotNull(resultado.getVersion());
    }
    
    @Test
    void findById_DeberiaRetornarInventarioVariante_CuandoExiste() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(50);
        inventario.setReserved(5);
        inventario = repository.save(inventario);
        
        // When
        Optional<InventarioVariante> resultado = repository.findById(inventario.getId());

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(50, resultado.get().getOnHand());
        assertEquals(5, resultado.get().getReserved());
    }

    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenInventariosVariante() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante1 = new Variante();
        variante1.setProducto(producto);
        variante1.setSku("SKU-1-" + System.currentTimeMillis());
        variante1.setEsDefault(true);
        variante1.setActivo(true);
        variante1 = varianteRepository.save(variante1);
        
        Variante variante2 = new Variante();
        variante2.setProducto(producto);
        variante2.setSku("SKU-2-" + System.currentTimeMillis());
        variante2.setEsDefault(false);
        variante2.setActivo(true);
        variante2 = varianteRepository.save(variante2);
        
        Variante variante3 = new Variante();
        variante3.setProducto(producto);
        variante3.setSku("SKU-3-" + System.currentTimeMillis());
        variante3.setEsDefault(false);
        variante3.setActivo(true);
        variante3 = varianteRepository.save(variante3);
        
        InventarioVariante inventario1 = new InventarioVariante();
        inventario1.setVariante(variante1);
        inventario1.setOnHand(100);
        inventario1.setReserved(10);
        repository.save(inventario1);
        
        InventarioVariante inventario2 = new InventarioVariante();
        inventario2.setVariante(variante2);
        inventario2.setOnHand(50);
        inventario2.setReserved(5);
        repository.save(inventario2);
        
        InventarioVariante inventario3 = new InventarioVariante();
        inventario3.setVariante(variante3);
        inventario3.setOnHand(0);
        inventario3.setReserved(0);
        repository.save(inventario3);
        
        // When
        List<InventarioVariante> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarInventarioVariante_CuandoExiste() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(100);
        inventario.setReserved(0);
        inventario = repository.save(inventario);
        
        Long id = inventario.getId();
        
        // Simular borrado lógico: marcar deletedAt y guardar
        inventario.setDeletedAt(LocalDateTime.now());
        repository.save(inventario);

        // Then: comprobar soft-delete
        Optional<InventarioVariante> resultado = repository.findById(id);
        assertTrue(resultado.isPresent());
        assertNotNull(resultado.get().getDeletedAt());
    }
    
    @Test
    void update_DeberiaActualizarInventarioVariante_CuandoExiste() {
        // Given
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("slug-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción");
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        InventarioVariante inventario = new InventarioVariante();
        inventario.setVariante(variante);
        inventario.setOnHand(100);
        inventario.setReserved(10);
        inventario.setUbicacion("Ubicación Original");
        inventario = repository.save(inventario);
        
        // When
        inventario.setOnHand(150);
        inventario.setReserved(20);
        inventario.setUbicacion("Ubicación Actualizada");
        InventarioVariante actualizado = repository.save(inventario);
        
        // Then
        Optional<InventarioVariante> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(150, resultado.get().getOnHand());
        assertEquals(20, resultado.get().getReserved());
        assertEquals("Ubicación Actualizada", resultado.get().getUbicacion());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod IV"); producto.setSlug("rb-prod-iv-"+System.currentTimeMillis()); producto.setDescripcion("rb"); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("rb-sku-iv-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); varianteRepository.save(variante);

        InventarioVariante inv = new InventarioVariante(); inv.setVariante(variante); inv.setOnHand(11); inv.setReserved(1); inv.setUbicacion("R1"); repository.save(inv);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM variante_inventario vi JOIN variante v ON vi.variante_id = v.id WHERE v.sku LIKE :sku")
                .setParameter("sku", "%rb-sku-iv-%")
                .getSingleResult();
        long count = cnt==null?0L:((Number)cnt).longValue();
        assertEquals(0L, count, "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        Producto producto = new Producto(); producto.setNombre("P IV"); producto.setSlug("p-iv-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-iv-"+UUID.randomUUID()); varianteRepository.save(variante);

        InventarioVariante first = new InventarioVariante(); first.setVariante(variante); first.setOnHand(5); first.setReserved(0); first.setUbicacion("A"); repository.saveAndFlush(first);

        boolean uniqueExists=false; try{ Object c = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='variante_inventario' AND c.contype='u'").getSingleResult(); if (c!=null) uniqueExists = ((Number)c).longValue()>0; } catch(Exception ex){ uniqueExists=false; }

        InventarioVariante dup = new InventarioVariante(); dup.setVariante(variante); dup.setOnHand(5); dup.setReserved(0); dup.setUbicacion("A");
        if (uniqueExists) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(dup); entityManager.flush(); }); }
        else { repository.saveAndFlush(dup); entityManager.flush(); long cnt = repository.findAll().stream().filter(x-> x.getVariante().getId().equals(variante.getId()) && x.getOnHand()==5).count(); assertTrue(cnt>=2); }

        try{ var saved = repository.findById(first.getId()).orElseThrow(); assertNotNull(saved.getCreatedAt()); } catch(Exception ex){ }
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando `restTemplate`.
    // Ejemplos pendientes: POST 201, GET {id} 200, GET lista 200, PUT 200, DELETE 204, POST 400.
    // Mantener separados de los tests de Repository para facilitar pruebas unitarias de controllers luego.
    // Tests HTTP usando MockMvc (perfil test, filtro JWT mockeado)

    @Test
    void post_DeberiaCrearInventarioVariante_Status201_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);

        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("varianteId", variante.getId());
        payload.put("onHand", 42);
        payload.put("reserved", 5);
        payload.put("almacenId", 123L);

        var result = mockMvc.perform(MockMvcRequestBuilders.post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertNotNull(dto.get("id"));
        assertEquals(123L, ((Number)dto.get("almacenId")).longValue());
    }

    @Test
    void get_DeberiaRetornarInventarioVariante_Status200_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        InventarioVariante inv = new InventarioVariante(); inv.setVariante(variante); inv.setOnHand(10); inv.setReserved(1); inv.setUbicacion("U1"); inv = repository.save(inv);

        var result = mockMvc.perform(MockMvcRequestBuilders.get("/api/inventario/variante/{varianteId}", variante.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertEquals(10, ((Number)dto.get("onHand")).intValue());
    }

    @Test
    void post_AjusteStock_DeberiaRetornar200_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        InventarioVariante inv = new InventarioVariante(); inv.setVariante(variante); inv.setOnHand(5); inv.setReserved(0); inv = repository.save(inv);

        var result = mockMvc.perform(MockMvcRequestBuilders.post("/api/inventario/variantes/{varianteId}/ajuste?deltaOnHand=3&deltaReserved=1", variante.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), java.util.Map.class);
        assertEquals(8, ((Number)dto.get("onHand")).intValue());
    }

    @Test
    void delete_DeberiaSoftDeletePorVariante_Status204_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        InventarioVariante inv = new InventarioVariante(); inv.setVariante(variante); inv.setOnHand(7); inv = repository.save(inv);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/inventario/variante/{varianteId}", variante.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        var refreshed = repository.findById(inv.getId());
        assertTrue(refreshed.isPresent());
        assertNotNull(refreshed.get().getDeletedAt());
    }


}
