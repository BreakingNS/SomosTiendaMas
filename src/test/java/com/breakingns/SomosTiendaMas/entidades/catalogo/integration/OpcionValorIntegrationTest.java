package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Opcion;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.OpcionValor;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.OpcionValorRepository;
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
public class OpcionValorIntegrationTest {
    
    @Autowired
    private OpcionValorRepository repository;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @BeforeEach
    void sincronizarSecuencia() {
        entityManager.createNativeQuery("SELECT setval('opcion_valor_id_seq', (SELECT COALESCE(MAX(id), 1) FROM opcion_valor))")
                     .getSingleResult();
    }
    
    // === TESTS DE REPOSITORY ===
    
    @Test
    void save_DeberiaCrearOpcionValor_CuandoDatosValidos() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Color");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Crear entidad con datos válidos
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Rojo");
        opcionValor.setSlug("rojo-test");
        opcionValor.setOrden(1);
        
        // Guardar en DB
        OpcionValor guardada = repository.save(opcionValor);
        
        // Verificar que ID fue generado
        assertNotNull(guardada.getId(), "El ID debe ser generado automáticamente");
        
        // Verificar que los datos se guardaron correctamente
        assertEquals("Rojo", guardada.getValor());
        assertEquals("rojo-test", guardada.getSlug());
        assertEquals(1, guardada.getOrden());
        assertNotNull(guardada.getOpcion());
    }
    
    @Test
    void findById_DeberiaRetornarOpcionValor_CuandoExiste() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Talla");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad en DB
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("M");
        opcionValor.setSlug("m-test");
        opcionValor.setOrden(2);
        OpcionValor guardada = repository.save(opcionValor);
        
        // Buscar por ID
        var resultado = repository.findById(guardada.getId());
        
        // Verificar que Optional.isPresent() es true
        assertTrue(resultado.isPresent(), "Debe encontrar el opción valor por ID");
        
        // Verificar datos coinciden
        OpcionValor encontrada = resultado.get();
        assertEquals("M", encontrada.getValor());
        assertEquals("m-test", encontrada.getSlug());
        assertEquals(2, encontrada.getOrden());
    }
    
    @Test
    void findAll_DeberiaRetornarLista_CuandoExistenOpcionValores() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Material");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar 3 entidades diferentes
        OpcionValor opcionValor1 = new OpcionValor();
        opcionValor1.setOpcion(opcion);
        opcionValor1.setValor("Algodón");
        opcionValor1.setSlug("algodon-test");
        opcionValor1.setOrden(1);
        repository.save(opcionValor1);
        
        OpcionValor opcionValor2 = new OpcionValor();
        opcionValor2.setOpcion(opcion);
        opcionValor2.setValor("Poliéster");
        opcionValor2.setSlug("poliester-test");
        opcionValor2.setOrden(2);
        repository.save(opcionValor2);
        
        OpcionValor opcionValor3 = new OpcionValor();
        opcionValor3.setOpcion(opcion);
        opcionValor3.setValor("Lana");
        opcionValor3.setSlug("lana-test");
        opcionValor3.setOrden(3);
        repository.save(opcionValor3);
        
        // Llamar findAll()
        var lista = repository.findAll();
        
        // Verificar que retorna 3+ elementos (puede haber datos previos)
        assertTrue(lista.size() >= 3, "Debe retornar al menos 3 opción valores");
    }
    
    @Test
    void delete_DeberiaEliminarOpcionValor_CuandoExiste() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Estilo");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("ParaBorrar");
        opcionValor.setSlug("para-borrar-test");
        opcionValor.setOrden(99);
        OpcionValor guardada = repository.save(opcionValor);
        Long id = guardada.getId();
        
        // Llamar deleteById
        repository.deleteById(id);
        
        // Verificar que repository.findById(id).isEmpty() es true
        assertTrue(repository.findById(id).isEmpty(), "El opción valor debe haber sido eliminado");
    }
    
    @Test
    void update_DeberiaActualizarOpcionValor_CuandoExiste() {
        // Crear opción padre
        Opcion opcion = new Opcion();
        opcion.setNombre("Acabado");
        opcion.setOrden(1);
        opcion.setTipo("select");
        opcion = opcionRepository.save(opcion);
        
        // Guardar entidad con valor "Original"
        OpcionValor opcionValor = new OpcionValor();
        opcionValor.setOpcion(opcion);
        opcionValor.setValor("Original");
        opcionValor.setSlug("original-test");
        opcionValor.setOrden(5);
        OpcionValor guardada = repository.save(opcionValor);
        
        // Modificar valor a "Actualizado"
        guardada.setValor("Actualizado");
        guardada.setSlug("actualizado-test");
        guardada.setOrden(10);
        
        // Llamar repository.save(entidadModificada)
        repository.save(guardada);
        
        // Buscar por ID y verificar valor es "Actualizado"
        var resultado = repository.findById(guardada.getId());
        assertTrue(resultado.isPresent());
        assertEquals("Actualizado", resultado.get().getValor());
        assertEquals("actualizado-test", resultado.get().getSlug());
        assertEquals(10, resultado.get().getOrden());
    }
    
}
