package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
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
public class VarianteIntegrationTest {
    
    @Autowired
    private VarianteRepository repository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('variante_id_seq', (SELECT COALESCE(MAX(id), 1) FROM variante))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearVariante_CuandoDatosValidos() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Variante Test");
        producto.setSlug("producto-variante-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Crear entidad con datos válidos
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-" + System.currentTimeMillis());
        variante.setEsDefault(true);
        variante.setActivo(true);
        
        // Guardar en DB
        Variante guardada = repository.save(variante);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertTrue(guardada.getSku().startsWith("SKU-"));
        assertTrue(guardada.isEsDefault());
        assertTrue(guardada.isActivo());
        assertNotNull(guardada.getProducto());
    }
    
    @Test
    void findById_DeberiaRetornarVariante_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 2");
        producto.setSlug("producto-2-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar entidad en DB
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-FIND-" + System.currentTimeMillis());
        variante.setEsDefault(false);
        variante.setActivo(true);
        Variante guardada = repository.save(variante);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la variante por ID");
        
        // Verificar datos coinciden
        Variante encontrada = resultado.get();
        assertTrue(encontrada.getSku().startsWith("SKU-FIND-"));
        assertFalse(encontrada.isEsDefault());
        assertTrue(encontrada.isActivo());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenVariantes() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 3");
        producto.setSlug("producto-3-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar 3 entidades diferentes
        Variante variante1 = new Variante();
        variante1.setProducto(producto);
        variante1.setSku("SKU-1-" + System.currentTimeMillis());
        variante1.setEsDefault(true);
        variante1.setActivo(true);
        repository.save(variante1);
        
        Variante variante2 = new Variante();
        variante2.setProducto(producto);
        variante2.setSku("SKU-2-" + System.currentTimeMillis());
        variante2.setEsDefault(false);
        variante2.setActivo(true);
        repository.save(variante2);
        
        Variante variante3 = new Variante();
        variante3.setProducto(producto);
        variante3.setSku("SKU-3-" + System.currentTimeMillis());
        variante3.setEsDefault(false);
        variante3.setActivo(false);
        repository.save(variante3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 variantes");
    }
    
    @Test
    void delete_DeberiaEliminarVariante_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 4");
        producto.setSlug("producto-4-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar entidad
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-DELETE-" + System.currentTimeMillis());
        variante.setEsDefault(false);
        variante.setActivo(true);
        Variante guardada = repository.save(variante);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La variante debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarVariante_CuandoExiste() {
        // Crear producto padre
        Producto producto = new Producto();
        producto.setNombre("Producto Test 5");
        producto.setSlug("producto-5-" + System.currentTimeMillis());
        producto = productoRepository.save(producto);
        
        // Guardar entidad con valores originales
        Variante variante = new Variante();
        variante.setProducto(producto);
        variante.setSku("SKU-ORIGINAL-" + System.currentTimeMillis());
        variante.setEsDefault(false);
        variante.setActivo(false);
        Variante guardada = repository.save(variante);
        
        // Modificar valores
        guardada.setEsDefault(true);
        guardada.setActivo(true);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar valores actualizados
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().isEsDefault());
        assertTrue(resultado.get().isActivo());
    }
    
}
