package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto_centralizado;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ProductoCrearDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCentralizadoCrearDTO {
    // Producto
    @NotNull(message = "producto es requerido")
    @Valid
    private ProductoCrearDTO producto;

    // Variantes
    /**
     * Variantes con recursos embebidos (opciones, precios, inventarios, physical, imagenes).
     */
    @NotNull(message = "variantes es requerido")
    @NotEmpty(message = "variantes debe contener al menos una variante")
    @Valid
    private List<VarianteAnidadaCrearDTO> variantes;

    // Getter/Setter explícitos para compatibilidad con IDEs sin Lombok
    public List<VarianteAnidadaCrearDTO> getVariantes() {
        return this.variantes;
    }

    public void setVariantes(List<VarianteAnidadaCrearDTO> variantes) {
        this.variantes = variantes;
    }
}