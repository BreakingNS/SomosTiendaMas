package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftCreateResponseDTO {
    private Long id;
    private String ownerId;
    private Long version;
}
