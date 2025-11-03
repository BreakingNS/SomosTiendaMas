package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.plantilla;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoAtributoDTO {
    private Long id;
    private Long productoId;
    private Long plantillaCampoId;
    private String nombre;
    private String slug;
    private String valor;
}
