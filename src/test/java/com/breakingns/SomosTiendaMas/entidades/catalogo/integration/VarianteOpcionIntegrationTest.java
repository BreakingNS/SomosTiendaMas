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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
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
    private TestRestTemplate restTemplate;
    
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
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La variante opción debe haber sido eliminada");
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
    
}
