package com.breakingns.SomosTiendaMas;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SomosTiendaMasApplication {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
    
    public static void main(String[] args) {
            SpringApplication.run(SomosTiendaMasApplication.class, args);
    }

}
