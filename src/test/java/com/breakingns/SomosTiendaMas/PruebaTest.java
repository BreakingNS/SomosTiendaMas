package com.breakingns.SomosTiendaMas;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class PruebaTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mockMvc_deberiaEstarInyectado() {
        assertNotNull(mockMvc);
    }
}
