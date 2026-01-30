package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class OpcionIntegrationTest {
    
    @Autowired
    private OpcionRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearOpcion_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Opcion opcion = new Opcion();
        opcion.setNombre("Color");
        opcion.setOrden(1);
        opcion.setTipo("select");
        
        // Guardar en DB
        Opcion guardada = repository.save(opcion);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Color", guardada.getNombre());
        assertEquals(1, guardada.getOrden());
        assertEquals("select", guardada.getTipo());
    }
    
    @Test
    void findById_DeberiaRetornarOpcion_CuandoExiste() {
        // Guardar entidad en DB
        Opcion opcion = new Opcion();
        opcion.setNombre("Talla");
        opcion.setOrden(2);
        opcion.setTipo("radio");
        Opcion guardada = repository.save(opcion);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la opción por ID");
        
        // Verificar datos coinciden
        Opcion encontrada = resultado.get();
        assertEquals("Talla", encontrada.getNombre());
        assertEquals(2, encontrada.getOrden());
        assertEquals("radio", encontrada.getTipo());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenOpciones() {
        // Guardar 3 entidades diferentes
        Opcion opcion1 = new Opcion();
        opcion1.setNombre("Primera");
        opcion1.setOrden(1);
        opcion1.setTipo("select");
        repository.save(opcion1);
        
        Opcion opcion2 = new Opcion();
        opcion2.setNombre("Segunda");
        opcion2.setOrden(2);
        opcion2.setTipo("checkbox");
        repository.save(opcion2);
        
        Opcion opcion3 = new Opcion();
        opcion3.setNombre("Tercera");
        opcion3.setOrden(3);
        opcion3.setTipo("radio");
        repository.save(opcion3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 opciones");
    }
    
    @Test
    void delete_DeberiaEliminarOpcion_CuandoExiste() {
        // Guardar entidad
        Opcion opcion = new Opcion();
        opcion.setNombre("ParaBorrar");
        opcion.setOrden(99);
        opcion.setTipo("text");
        Opcion guardada = repository.save(opcion);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La opción debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarOpcion_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Opcion opcion = new Opcion();
        opcion.setNombre("Original");
        opcion.setOrden(5);
        opcion.setTipo("select");
        Opcion guardada = repository.save(opcion);
        
        // Modificar nombre a "Actualizado"
        guardada.setNombre("Actualizado");
        guardada.setOrden(10);
        guardada.setTipo("checkbox");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals(10, resultado.get().getOrden());
        assertEquals("checkbox", resultado.get().getTipo());
    }
    
}
