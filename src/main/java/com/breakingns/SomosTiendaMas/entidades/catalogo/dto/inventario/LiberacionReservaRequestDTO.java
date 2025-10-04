package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LiberacionReservaRequestDTO {
    @NotNull
    private String orderRef;
}
