package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftPatchRequestDTO {
    private DraftData data;
    private Integer step;
    private Long expectedVersion;
}