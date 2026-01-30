package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Categoria;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class CategoriaIntegrationTest {
    
    @Autowired
    private CategoriaRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('categoria_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categoria))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearCategoria_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Categoria categoria = new Categoria();
        categoria.setNombre("ejemplo");
        categoria.setSlug("ejemplo-test");
        categoria.setDescripcion("Descripción de ejemplo");
        
        // Guardar en DB
        Categoria guardada = repository.save(categoria);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("ejemplo", guardada.getNombre());
        assertEquals("ejemplo-test", guardada.getSlug());
        assertEquals("Descripción de ejemplo", guardada.getDescripcion());
    }
    
    @Test
    void findById_DeberiaRetornarCategoria_CuandoExiste() {
        // Guardar entidad en DB
        Categoria categoria = new Categoria();
        categoria.setNombre("Ropa");
        categoria.setSlug("ropa-test");
        categoria.setDescripcion("Prendas de vestir");
        Categoria guardada = repository.save(categoria);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la categoría por ID");
        
        // Verificar datos coinciden
        Categoria encontrada = resultado.get();
        assertEquals("Ropa", encontrada.getNombre());
        assertEquals("ropa-test", encontrada.getSlug());
        assertEquals("Prendas de vestir", encontrada.getDescripcion());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenCategorias() {
        // Guardar 3 entidades diferentes
        Categoria categoria1 = new Categoria();
        categoria1.setNombre("Primera");
        categoria1.setSlug("primera-test");
        categoria1.setDescripcion("Primera categoría");
        repository.save(categoria1);
        
        Categoria categoria2 = new Categoria();
        categoria2.setNombre("Segunda");
        categoria2.setSlug("segunda-test");
        categoria2.setDescripcion("Segunda categoría");
        repository.save(categoria2);
        
        Categoria categoria3 = new Categoria();
        categoria3.setNombre("Tercera");
        categoria3.setSlug("tercera-test");
        categoria3.setDescripcion("Tercera categoría");
        repository.save(categoria3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 categorías");
    }
    
    @Test
    void delete_DeberiaEliminarCategoria_CuandoExiste() {
        // Guardar entidad
        Categoria categoria = new Categoria();
        categoria.setNombre("ParaBorrar");
        categoria.setSlug("para-borrar-test");
        categoria.setDescripcion("Para eliminar");
        Categoria guardada = repository.save(categoria);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La categoría debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarCategoria_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Categoria categoria = new Categoria();
        categoria.setNombre("Original");
        categoria.setSlug("original-test");
        categoria.setDescripcion("Descripción original");
        Categoria guardada = repository.save(categoria);
        
        // Modificar nombre a "Actualizado"
        guardada.setNombre("Actualizado");
        guardada.setSlug("actualizado-test");
        guardada.setDescripcion("Descripción actualizada");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("actualizado-test", resultado.get().getSlug());
        assertEquals("Descripción actualizada", resultado.get().getDescripcion());
    }
    
}
