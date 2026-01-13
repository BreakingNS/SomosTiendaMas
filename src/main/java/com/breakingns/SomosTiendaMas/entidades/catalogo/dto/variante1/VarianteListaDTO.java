package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante1;

import lombok.Data;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.precio.PrecioVarianteResumenDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.inventario.DisponibilidadResponseDTO;
import java.util.List;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.variante_opcion.OpcionConValoresDTO;

@Data
public class VarianteListaDTO {
    private Long id;
    private Long productoId;
    private String skuResuelto;
    private Long precioCentavos;
    private PrecioVarianteResumenDTO precio;
    private DisponibilidadResponseDTO disponible;
    private Boolean esDefault;
    private Boolean activo;
    private List<OpcionConValoresDTO> opciones;
}
