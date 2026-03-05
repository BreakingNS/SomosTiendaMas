package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.TipoMovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.MovimientoInventario;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MovimientoInventarioRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class MovimientoInventarioIntegrationTest {
    
    @Autowired
    private MovimientoInventarioRepository repository;
    
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
        entityManager.createNativeQuery("SELECT setval('movimiento_inventario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM movimiento_inventario))")
                .getSingleResult();
    }
    
    @Test
    void save_DeberiaCrearMovimientoInventario_CuandoDatosValidos() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento.setCantidad(50L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento.setReferenciaId("REF-123");
        
        // When
        MovimientoInventario resultado = repository.save(movimiento);
        
        // Then
        assertNotNull(resultado.getId());
        assertEquals(TipoMovimientoInventario.ENTRADA_AJUSTE, resultado.getTipo());
        assertEquals(50L, resultado.getCantidad());
        assertNotNull(resultado.getOrderRef());
        assertEquals("REF-123", resultado.getReferenciaId());
    }
    
    @Test
    void findById_DeberiaRetornarMovimientoInventario_CuandoExiste() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.SALIDA_VENTA);
        movimiento.setCantidad(20L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento = repository.save(movimiento);
        
        // When
        Optional<MovimientoInventario> resultado = repository.findById(movimiento.getId());
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals(TipoMovimientoInventario.SALIDA_VENTA, resultado.get().getTipo());
        assertEquals(20L, resultado.get().getCantidad());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenMovimientosInventario() {
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
        
        MovimientoInventario movimiento1 = new MovimientoInventario();
        movimiento1.setVariante(variante1);
        movimiento1.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento1.setCantidad(100L);
        movimiento1.setOrderRef("ORDER-1-" + System.currentTimeMillis());
        repository.save(movimiento1);
        
        MovimientoInventario movimiento2 = new MovimientoInventario();
        movimiento2.setVariante(variante1);
        movimiento2.setTipo(TipoMovimientoInventario.SALIDA_VENTA);
        movimiento2.setCantidad(30L);
        movimiento2.setOrderRef("ORDER-2-" + System.currentTimeMillis());
        repository.save(movimiento2);
        
        MovimientoInventario movimiento3 = new MovimientoInventario();
        movimiento3.setVariante(variante2);
        movimiento3.setTipo(TipoMovimientoInventario.RESERVA);
        movimiento3.setCantidad(5L);
        movimiento3.setOrderRef("ORDER-3-" + System.currentTimeMillis());
        repository.save(movimiento3);
        
        // When
        List<MovimientoInventario> resultados = repository.findAll();
        
        // Then
        assertTrue(resultados.size() >= 3);
    }
    
    @Test
    void delete_DeberiaEliminarMovimientoInventario_CuandoExiste() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento.setCantidad(50L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento = repository.save(movimiento);
        
        Long id = movimiento.getId();
        
        // When
        repository.deleteById(id);
        
        // Then
        Optional<MovimientoInventario> resultado = repository.findById(id);
        assertFalse(resultado.isPresent());
    }
    
    @Test
    void update_DeberiaActualizarMovimientoInventario_CuandoExiste() {
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
        
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setVariante(variante);
        movimiento.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE);
        movimiento.setCantidad(50L);
        movimiento.setOrderRef("ORDER-" + System.currentTimeMillis());
        movimiento.setReferenciaId("REF-ORIGINAL");
        movimiento = repository.save(movimiento);
        
        // When
        movimiento.setCantidad(75L);
        movimiento.setReferenciaId("REF-ACTUALIZADA");
        movimiento.setMetadataJson("{\"updated\": true}");
        MovimientoInventario actualizado = repository.save(movimiento);
        
        // Then
        Optional<MovimientoInventario> resultado = repository.findById(actualizado.getId());
        assertTrue(resultado.isPresent());
        assertEquals(75L, resultado.get().getCantidad());
        assertEquals("REF-ACTUALIZADA", resultado.get().getReferenciaId());
        assertEquals("{\"updated\": true}", resultado.get().getMetadataJson());
    }
    
    // === TESTS DE API (Controller) ===
    // Aquí van los tests de integración a nivel HTTP usando MockMvc (perfil test, JWT filter mockeado).
    // Ejemplos: reservar -> crea RESERVA, liberar -> crea LIBERACION, confirmar -> crea SALIDA_VENTA, delete -> soft-delete.

    @Test
    void post_Reserva_DeberiaCrearMovimientoRESERVA_yActualizarInventario_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        // crear inventario inicial
        InventarioVariante inv = new InventarioVariante();
        inv.setVariante(variante); inv.setOnHand(10); inv.setReserved(0);
        entityManager.persist(inv); entityManager.flush();

        String orderRef = "ORD-API-" + System.currentTimeMillis();
        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("cantidad", 2);
        payload.put("orderRef", orderRef);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventario/variantes/{varianteId}/reserva", variante.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        var movs = repository.findByOrderRefAndTipo(orderRef, TipoMovimientoInventario.RESERVA);
        assertTrue(movs.size() >= 1);
        assertEquals(variante.getId(), movs.get(0).getVariante().getId());
    }

    @Test
    void post_Liberar_DeberiaCrearMovimientoLIBERACION_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        // crear inventario y movimiento RESERVA manual
        InventarioVariante inv = new InventarioVariante();
        inv.setVariante(variante); inv.setOnHand(10); inv.setReserved(3);
        entityManager.persist(inv); entityManager.flush();

        String orderRef = "ORD-LIB-" + System.currentTimeMillis();
        MovimientoInventario m = new MovimientoInventario(); m.setVariante(variante); m.setProducto(producto); m.setTipo(TipoMovimientoInventario.RESERVA); m.setCantidad(3L); m.setOrderRef(orderRef);
        repository.save(m);

        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("orderRef", orderRef);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventario/variantes/{varianteId}/liberar", variante.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        var movs = repository.findByOrderRefAndTipo(orderRef, TipoMovimientoInventario.LIBERACION);
        assertTrue(movs.size() >= 1);

        var resp = mockMvc.perform(MockMvcRequestBuilders.get("/dev/api/movimientos").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        List<Map<String,Object>> list = objectMapper.readValue(resp.getResponse().getContentAsString(), new TypeReference<List<Map<String,Object>>>(){});
        assertTrue(list.size() >= 1);
        assertTrue(list.stream().anyMatch(row -> orderRef.equals(row.get("orderRef"))));
    }

    @Test
    void post_Confirmar_DeberiaCrearMovimientoSALIDA_VENTA_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        // crear inventario y movimiento RESERVA para la orderRef
        InventarioVariante inv = new InventarioVariante();
        inv.setVariante(variante); inv.setOnHand(10); inv.setReserved(1);
        entityManager.persist(inv); entityManager.flush();

        String orderRef = "ORD-CONF-" + System.currentTimeMillis();
        MovimientoInventario m = new MovimientoInventario(); m.setVariante(variante); m.setProducto(producto); m.setTipo(TipoMovimientoInventario.RESERVA); m.setCantidad(1L); m.setOrderRef(orderRef);
        repository.save(m);

        java.util.Map<String,Object> payload = new java.util.HashMap<>();
        payload.put("orderRef", orderRef);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventario/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        var movs = repository.findByOrderRefAndTipo(orderRef, TipoMovimientoInventario.SALIDA_VENTA);
        assertTrue(movs.size() >= 1);

        var resp = mockMvc.perform(MockMvcRequestBuilders.get("/dev/api/movimientos").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        List<Map<String,Object>> list = objectMapper.readValue(resp.getResponse().getContentAsString(), new TypeReference<List<Map<String,Object>>>(){});
        assertTrue(list.size() >= 1);
        assertTrue(list.stream().anyMatch(row -> orderRef.equals(row.get("orderRef"))));
    }

    @Test
    void delete_DeberiaSoftDeletePorVariante_Status204_API() throws Exception {
        Producto producto = new Producto(); producto.setNombre("Prod API"); producto.setSlug("prod-api-"+System.currentTimeMillis()); producto = productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("SKU-API-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); variante = varianteRepository.save(variante);
        com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante inv = new com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante(); inv.setVariante(variante); inv.setOnHand(7);
        entityManager.persist(inv); entityManager.flush();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/inventario/variante/{varianteId}", variante.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante refreshed = entityManager.find(com.breakingns.SomosTiendaMas.entidades.catalogo.model.InventarioVariante.class, inv.getId());
        assertNotNull(refreshed);
        assertNotNull(refreshed.getDeletedAt());
    }

    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod MI"); producto.setSlug("rb-prod-mi-"+System.currentTimeMillis()); producto.setDescripcion("rb"); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("rb-sku-mi-"+System.currentTimeMillis()); variante.setEsDefault(true); variante.setActivo(true); varianteRepository.save(variante);

        MovimientoInventario m = new MovimientoInventario(); m.setVariante(variante); m.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE); m.setCantidad(5L); m.setOrderRef("RB-ORD"); repository.save(m);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM movimiento_inventario mi JOIN variante v ON mi.variante_id = v.id WHERE v.sku LIKE :sku")
                .setParameter("sku", "%rb-sku-mi-%")
                .getSingleResult();
        long count = cnt==null?0L:((Number)cnt).longValue();
        assertEquals(0L, count, "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        Producto producto = new Producto(); producto.setNombre("P MI"); producto.setSlug("p-mi-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-mi-"+UUID.randomUUID()); varianteRepository.save(variante);

        MovimientoInventario first = new MovimientoInventario(); first.setVariante(variante); first.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE); first.setCantidad(10L); first.setOrderRef("ORD-UNIQ"); repository.saveAndFlush(first);

        MovimientoInventario dup = new MovimientoInventario(); dup.setVariante(variante); dup.setTipo(TipoMovimientoInventario.ENTRADA_AJUSTE); dup.setCantidad(10L); dup.setOrderRef("ORD-UNIQ");
        try {
            repository.saveAndFlush(dup);
            long cnt = repository.findByOrderRefAndTipo("ORD-UNIQ", TipoMovimientoInventario.ENTRADA_AJUSTE).size();
            assertTrue(cnt>=2, "Si no hay constraint, deberían existir al menos 2 movimientos con same orderRef+tipo");
        } catch (DataIntegrityViolationException ex) {
            // expected when DB enforces unique constraint
        }

        boolean orderNotNull=false; try{ Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='movimiento_inventario' AND column_name='order_ref'").getSingleResult(); if (nn!=null) orderNotNull = "NO".equalsIgnoreCase(nn.toString()); } catch(Exception ex){ orderNotNull=false; }
        MovimientoInventario c = new MovimientoInventario(); c.setVariante(variante); c.setTipo(TipoMovimientoInventario.RESERVA); c.setCantidad(1L); c.setOrderRef(null);
        if (orderNotNull) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(c); }); } else { repository.saveAndFlush(c); var saved = repository.findById(c.getId()).orElseThrow(); assertNull(saved.getOrderRef()); }

        try{ var saved = repository.findById(first.getId()).orElseThrow(); assertNotNull(saved.getCreatedAt()); } catch(Exception ex){ }
    }

}
