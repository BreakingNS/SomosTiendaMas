package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Etiqueta;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.EtiquetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class EtiquetaIntegrationTest {
    
    @Autowired
    private EtiquetaRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearEtiqueta_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Oferta");
        etiqueta.setSlug("oferta-test");
        
        // Guardar en DB
        Etiqueta guardada = repository.save(etiqueta);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Oferta", guardada.getNombre());
        assertEquals("oferta-test", guardada.getSlug());
    }
    
    @Test
    void findById_DeberiaRetornarEtiqueta_CuandoExiste() {
        // Guardar entidad en DB
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Nueva");
        etiqueta.setSlug("nueva-test");
        Etiqueta guardada = repository.save(etiqueta);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la etiqueta por ID");
        
        // Verificar datos coinciden
        Etiqueta encontrada = resultado.get();
        assertEquals("Nueva", encontrada.getNombre());
        assertEquals("nueva-test", encontrada.getSlug());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenEtiquetas() {
        // Guardar 3 entidades diferentes
        Etiqueta etiqueta1 = new Etiqueta();
        etiqueta1.setNombre("Primera");
        etiqueta1.setSlug("primera-test");
        repository.save(etiqueta1);
        
        Etiqueta etiqueta2 = new Etiqueta();
        etiqueta2.setNombre("Segunda");
        etiqueta2.setSlug("segunda-test");
        repository.save(etiqueta2);
        
        Etiqueta etiqueta3 = new Etiqueta();
        etiqueta3.setNombre("Tercera");
        etiqueta3.setSlug("tercera-test");
        repository.save(etiqueta3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 etiquetas");
    }
    
    @Test
    void delete_DeberiaEliminarEtiqueta_CuandoExiste() {
        // Guardar entidad
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("ParaBorrar");
        etiqueta.setSlug("para-borrar-test");
        Etiqueta guardada = repository.save(etiqueta);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La etiqueta debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarEtiqueta_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Original");
        etiqueta.setSlug("original-test");
        Etiqueta guardada = repository.save(etiqueta);
        
        // Modificar nombre a "Actualizado"
        guardada.setNombre("Actualizado");
        guardada.setSlug("actualizado-test");
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar nombre es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getNombre());
        assertEquals("actualizado-test", resultado.get().getSlug());
    }
    
}
