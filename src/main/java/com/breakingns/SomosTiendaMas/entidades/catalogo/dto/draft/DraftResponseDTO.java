package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftResponseDTO {
    private Long id;
    private String ownerId;
    private String status;
    private Integer step;
    private DraftData data;
    private String metaJson;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime committedAt;
}