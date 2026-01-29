package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperacionSimpleResponseDTO {
    private boolean ok;
    private String message;

    public OperacionSimpleResponseDTO(boolean ok) {
        this.ok = ok;
    }
}
