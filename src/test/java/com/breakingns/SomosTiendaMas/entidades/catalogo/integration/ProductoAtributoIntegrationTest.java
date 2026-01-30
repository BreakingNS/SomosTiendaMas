package com.breakingns.SomosTiendaMas.entidades.catalogo.integration;

import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoAtributoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ProductoAtributoIntegrationTest {
    
    @Autowired
    private ProductoAtributoRepository repository;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    // Tests pendientes
    
}
