package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
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
public class ProductoIntegrationTest {
    
    @Autowired
    private ProductoRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('producto_id_seq', (SELECT COALESCE(MAX(id), 1) FROM producto))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearProducto_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Producto producto = new Producto();
        producto.setNombre("Producto Test");
        producto.setSlug("producto-test-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción de prueba");
        
        // Guardar en DB
        Producto guardado = repository.save(producto);
        
        // Verificar que ID fue generado
        assertNotNull(guardado.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Producto Test", guardado.getNombre());
        assertTrue(guardado.getSlug().startsWith("producto-test-"));
        assertEquals("Descripción de prueba", guardado.getDescripcion());
    }
    
    @Test
    void findById_DeberiaRetornarProducto_CuandoExiste() {
        // Guardar entidad en DB
        Producto producto = new Producto();
        producto.setNombre("Producto Nuevo");
        producto.setSlug("producto-nuevo-" + System.currentTimeMillis());
        producto.setDescripcion("Nuevo producto");
        Producto guardado = repository.save(producto);
        
        // Buscar por ID
        var resultado = repository.findById(guardado.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el producto por ID");
        
        // Verificar datos coinciden
        Producto encontrado = resultado.get();
        assertEquals("Producto Nuevo", encontrado.getNombre());
        assertTrue(encontrado.getSlug().startsWith("producto-nuevo-"));
        assertEquals("Nuevo producto", encontrado.getDescripcion());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenProductos() {
        // Guardar 3 entidades diferentes
        Producto producto1 = new Producto();
        producto1.setNombre("Producto 1");
        producto1.setSlug("producto-1-" + System.currentTimeMillis());
        producto1.setDescripcion("Descripción 1");
        repository.save(producto1);
        
        Producto producto2 = new Producto();
        producto2.setNombre("Producto 2");
        producto2.setSlug("producto-2-" + System.currentTimeMillis());
        producto2.setDescripcion("Descripción 2");
        repository.save(producto2);
        
        Producto producto3 = new Producto();
        producto3.setNombre("Producto 3");
        producto3.setSlug("producto-3-" + System.currentTimeMillis());
        producto3.setDescripcion("Descripción 3");
        repository.save(producto3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 productos");
    }
    
    @Test
    void delete_DeberiaEliminarProducto_CuandoExiste() {
        // Guardar entidad
        Producto producto = new Producto();
        producto.setNombre("ParaBorrar");
        producto.setSlug("para-borrar-" + System.currentTimeMillis());
        producto.setDescripcion("Para eliminar");
        Producto guardado = repository.save(producto);
        Long id = guardado.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El producto debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarProducto_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Producto producto = new Producto();
        producto.setNombre("Original");
        producto.setSlug("original-" + System.currentTimeMillis());
        producto.setDescripcion("Descripción original");
        Producto guardado = repository.save(producto);
        
        // Modificar nombre a "Actualizado"
        guardado.setNombre("Actualizado");
        guardado.setDescripcion("Descripción actualizada");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardado);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("Descripción actualizada", resultado.get().getDescripcion());
    }
    
}
