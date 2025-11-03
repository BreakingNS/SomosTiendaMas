package com.breakingns.SomosTiendaMas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final UploadProperties props;

    public StaticResourceConfig(UploadProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve archivos subidos desde /uploads/**
        String location = props.getRoot().toUri().toString();
        registry.addResourceHandler(props.getUrlBase() + "/**")
                .addResourceLocations(location)
                .setCachePeriod(31536000); // cache 1 año
    }
}
