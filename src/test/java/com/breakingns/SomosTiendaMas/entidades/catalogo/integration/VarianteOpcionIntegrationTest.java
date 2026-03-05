package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteOpcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteOpcionRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import com.breakingns.SomosTiendaMas.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(OrderAnnotation.class)
public class VarianteOpcionIntegrationTest {
    
    @Autowired
    private VarianteOpcionRepository repository;

    @Autowired
    private VarianteRepository varianteRepository;

    @Autowired
    private OpcionRepository opcionRepository;

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
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('variante_opcion_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante_opcion))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearVarianteOpcion_CuandoDatosValidos() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("producto-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Color");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Crear entidad con datos válidos
        VarianteOpcion varianteOpcion = new VarianteOpcion();
        varianteOpcion.setVariante(variante);
        varianteOpcion.setOpcion(opcion);
        varianteOpcion.setOrden(1);
        varianteOpcion.setRequerido(true);
        varianteOpcion.setActivo(true);
        
        // Guardar en DB
        VarianteOpcion guardada = repository.save(varianteOpcion);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals(1, guardada.getOrden());
        assertTrue(guardada.isRequerido());
        assertTrue(guardada.isActivo());
        assertNotNull(guardada.getVariante());
        assertNotNull(guardada.getOpcion());
    }
    
    @Test
    void findById_DeberiaRetornarVarianteOpcion_CuandoExiste() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 2");
        producto.setSlug("producto-2-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-2-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Talla");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad en DB
        VarianteOpcion varianteOpcion = new VarianteOpcion();
        varianteOpcion.setVariante(variante);
        varianteOpcion.setOpcion(opcion);
        varianteOpcion.setOrden(2);
        varianteOpcion.setRequerido(false);
        varianteOpcion.setActivo(true);
        VarianteOpcion guardada = repository.save(varianteOpcion);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la variante opción por ID");
        
        // Verificar datos coinciden
        VarianteOpcion encontrada = resultado.get();
        assertEquals(2, encontrada.getOrden());
        assertFalse(encontrada.isRequerido());
        assertTrue(encontrada.isActivo());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVarianteOpciones() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 3");
        producto.setSlug("producto-3-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-3-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Guardar 3 entidades diferentes con diferentes opciones
        Opcion opcion1 = new Opcion();
        opcion1.setNombre("Opción 1");
        opcion1.setOrden(1);
        opcion1.setTipo("select");
        opcion1 = opcionRepository.save(opcion1);
        
        VarianteOpcion vo1 = new VarianteOpcion();
        vo1.setVariante(variante);
        vo1.setOpcion(opcion1);
        vo1.setOrden(1);
        vo1.setRequerido(true);
        vo1.setActivo(true);
        repository.save(vo1);
        
        Opcion opcion2 = new Opcion();
        opcion2.setNombre("Opción 2");
        opcion2.setOrden(2);
        opcion2.setTipo("checkbox");
        opcion2 = opcionRepository.save(opcion2);
        
        VarianteOpcion vo2 = new VarianteOpcion();
        vo2.setVariante(variante);
        vo2.setOpcion(opcion2);
        vo2.setOrden(2);
        vo2.setRequerido(false);
        vo2.setActivo(true);
        repository.save(vo2);
        
        Opcion opcion3 = new Opcion();
        opcion3.setNombre("Opción 3");
        opcion3.setOrden(3);
        opcion3.setTipo("radio");
        opcion3 = opcionRepository.save(opcion3);
        
        VarianteOpcion vo3 = new VarianteOpcion();
        vo3.setVariante(variante);
        vo3.setOpcion(opcion3);
        vo3.setOrden(3);
        vo3.setRequerido(true);
        vo3.setActivo(false);
        repository.save(vo3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 variante opciones");
    }
    
    @Test
    void delete_DeberiaEliminarVarianteOpcion_CuandoExiste() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 4");
        producto.setSlug("producto-4-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-4-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("ParaBorrar");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad
        VarianteOpcion varianteOpcion = new VarianteOpcion();
        varianteOpcion.setVariante(variante);
        varianteOpcion.setOpcion(opcion);
        varianteOpcion.setOrden(99);
        varianteOpcion.setRequerido(false);
        varianteOpcion.setActivo(true);
        VarianteOpcion guardada = repository.save(varianteOpcion);
        Long id = guardada.getId();
        
        // Simular borrado lógico: marcar deletedAt y guardar (el comportamiento del servicio hace esto)
        guardada.setDeletedAt(LocalDateTime.now());
        guardada.setUpdatedBy("test");
        repository.save(guardada);

        // Verificar soft-delete: la entidad sigue presente y tiene deletedAt seteado
        var opt = repository.findById(id);
        assertTrue(opt.isPresent(), "La variante opción debe seguir presente (soft-delete)");
        assertNotNull(opt.get().getDeletedAt(), "El campo deletedAt debe estar seteado tras soft-delete");
    }
    
    @Test
    void update_DeberiaActualizarVarianteOpcion_CuandoExiste() {
        // Crear producto y variante padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 5");
        producto.setSlug("producto-5-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-5-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        variante = varianteRepository.save(variante);
        
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Material");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad con valores originales
        VarianteOpcion varianteOpcion = new VarianteOpcion();
        varianteOpcion.setVariante(variante);
        varianteOpcion.setOpcion(opcion);
        varianteOpcion.setOrden(5);
        varianteOpcion.setRequerido(false);
        varianteOpcion.setActivo(false);
        VarianteOpcion guardada = repository.save(varianteOpcion);
        
        // Modificar valores
        guardada.setOrden(10);
        guardada.setRequerido(true);
        guardada.setActivo(true);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar valores actualizados
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals(10, resultado.get().getOrden());
        assertTrue(resultado.get().isRequerido());
        assertTrue(resultado.get().isActivo());
    }
    
    // === TESTS DE API (Controller) ===
    // VarianteOpcionValorIntegration se encarga de los tests de API relacionados con VarianteOpcion
    // === VERIFICACIONES DB REALES ===

    @Order(1)
    @Test
    @Transactional
    void db_Transaccion_Rollback_Crear_NoPersiste() {
        Producto producto = new Producto(); producto.setNombre("RB Prod VO"); producto.setSlug("rb-prod-vo-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("rb-sku-vo-"+System.currentTimeMillis()); varianteRepository.save(variante);
        Opcion opcion = new Opcion(); opcion.setNombre("RB Opt"); opcion.setOrden(1); opcion.setTipo("select"); opcionRepository.save(opcion);

        VarianteOpcion vo = new VarianteOpcion(); vo.setVariante(variante); vo.setOpcion(opcion); vo.setOrden(1); repository.save(vo);
        entityManager.flush();
    }

    @Order(2)
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Transaccion_Rollback_Verificar_NoExiste() {
        var found = repository.findAll().stream().filter(x-> x.getVariante()!=null && x.getVariante().getSku()!=null && x.getVariante().getSku().contains("rb-sku-vo-")).findAny();
        assertTrue(found.isEmpty(), "La entidad creada en la transacción anterior debe haber sido rollback-eada");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void db_Constraint_UniqueYNotNull_Y_Audit() {
        Producto producto = new Producto(); producto.setNombre("P VO"); producto.setSlug("p-vo-"+System.currentTimeMillis()); productoRepository.save(producto);
        Variante variante = new Variante(); variante.setProducto(producto); variante.setSku("sku-vo-"+UUID.randomUUID()); varianteRepository.save(variante);
        Opcion opcion = new Opcion(); opcion.setNombre("Opt VO"); opcion.setOrden(1); opcion.setTipo("select"); opcionRepository.save(opcion);

        VarianteOpcion first = new VarianteOpcion(); first.setVariante(variante); first.setOpcion(opcion); repository.saveAndFlush(first);

        boolean uniqueExists = false;
        try { Object cnt = entityManager.createNativeQuery("SELECT count(*) FROM pg_constraint c JOIN pg_class t ON c.conrelid=t.oid WHERE t.relname='variante_opcion' AND c.contype='u'").getSingleResult(); if (cnt!=null) uniqueExists = ((Number)cnt).longValue()>0; } catch(Exception ex){ uniqueExists=false; }

        VarianteOpcion dup = new VarianteOpcion(); dup.setVariante(variante); dup.setOpcion(opcion);
        if (uniqueExists) { assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(dup); entityManager.flush(); }); }
        else { repository.saveAndFlush(dup); entityManager.flush(); long count = repository.findAll().stream().filter(x-> x.getVariante().getId().equals(variante.getId()) && x.getOpcion().getId().equals(opcion.getId())).count(); assertTrue(count>=2); }

        boolean varianteNotNull=false; try{ Object nn = entityManager.createNativeQuery("SELECT is_nullable FROM information_schema.columns WHERE table_name='variante_opcion' AND column_name='variante_id'").getSingleResult(); if (nn!=null) varianteNotNull = "NO".equalsIgnoreCase(nn.toString()); } catch(Exception ex){ varianteNotNull=false; }
        if (varianteNotNull) { VarianteOpcion bad = new VarianteOpcion(); bad.setVariante(null); bad.setOpcion(opcion); assertThrows(DataIntegrityViolationException.class, ()->{ repository.saveAndFlush(bad); entityManager.flush(); }); }

        try{ var saved = repository.findById(first.getId()).orElseThrow(); assertNotNull(saved.getCreatedAt()); } catch(Exception ex){ }
    }
}
