package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftListItemDTO {
    private Long id;
    private String ownerId;
    private String status;
    private Integer step;
    private Map<String, Object> meta;
    private Long version;
    private LocalDateTime updatedAt;
}
