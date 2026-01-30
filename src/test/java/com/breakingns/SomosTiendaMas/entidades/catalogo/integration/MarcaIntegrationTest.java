package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Marca;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.MarcaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MarcaIntegrationTest {
    
    @Autowired
    private MarcaRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearMarca_CuandoDatosValidos() {
        // Crear entidad con datos válidos
        Marca marca = new Marca();
        marca.setNombre("Nike");
        marca.setSlug("nike-test");
        marca.setDescripcion("Marca deportiva");
        
        // Guardar en DB
        Marca guardada = repository.save(marca);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Nike", guardada.getNombre());
        assertEquals("nike-test", guardada.getSlug());
        assertEquals("Marca deportiva", guardada.getDescripcion());
    }
    
    @Test
    void findById_DeberiaRetornarMarca_CuandoExiste() {
        // Guardar entidad en DB
        Marca marca = new Marca();
        marca.setNombre("Adidas");
        marca.setSlug("adidas-test");
        marca.setDescripcion("Marca de ropa");
        Marca guardada = repository.save(marca);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar la marca por ID");
        
        // Verificar datos coinciden
        Marca encontrada = resultado.get();
        assertEquals("Adidas", encontrada.getNombre());
        assertEquals("adidas-test", encontrada.getSlug());
        assertEquals("Marca de ropa", encontrada.getDescripcion());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenMarcas() {
        // Guardar 3 entidades diferentes
        Marca marca1 = new Marca();
        marca1.setNombre("Primera");
        marca1.setSlug("primera-test");
        marca1.setDescripcion("Primera marca");
        repository.save(marca1);
        
        Marca marca2 = new Marca();
        marca2.setNombre("Segunda");
        marca2.setSlug("segunda-test");
        marca2.setDescripcion("Segunda marca");
        repository.save(marca2);
        
        Marca marca3 = new Marca();
        marca3.setNombre("Tercera");
        marca3.setSlug("tercera-test");
        marca3.setDescripcion("Tercera marca");
        repository.save(marca3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 marcas");
    }
    
    @Test
    void delete_DeberiaEliminarMarca_CuandoExiste() {
        // Guardar entidad
        Marca marca = new Marca();
        marca.setNombre("ParaBorrar");
        marca.setSlug("para-borrar-test");
        marca.setDescripcion("Para eliminar");
        Marca guardada = repository.save(marca);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "La marca debe haber sido eliminada");
    }
    
    @Test
    void update_DeberiaActualizarMarca_CuandoExiste() {
        // Guardar entidad con nombre "Original"
        Marca marca = new Marca();
        marca.setNombre("Original");
        marca.setSlug("original-test");
        marca.setDescripcion("Descripción original");
        Marca guardada = repository.save(marca);
        
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
