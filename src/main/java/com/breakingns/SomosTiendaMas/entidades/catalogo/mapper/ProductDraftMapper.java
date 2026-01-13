package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftCreateResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftListItemDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductDraft;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductDraftMapper {

    private final ObjectMapper mapper;

    public ProductDraftMapper() {
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public DraftCreateResponseDTO toCreateResponse(ProductDraft draft) {
        if (draft == null) return null;
        return DraftCreateResponseDTO.builder()
                .id(draft.getId())
                .ownerId(draft.getOwnerId())
                .version(draft.getVersion())
                .build();
    }

    public DraftResponseDTO toResponse(ProductDraft draft) {
        if (draft == null) return null;
        DraftResponseDTO dto = DraftResponseDTO.builder()
                .id(draft.getId())
                .ownerId(draft.getOwnerId())
                .status(draft.getStatus() != null ? draft.getStatus().name() : null)
                .step(draft.getStep())
                .data(draft.getDraftData())
                .metaJson(draft.getMetaJson())
                .version(draft.getVersion())
                .createdAt(draft.getCreatedAt())
                .updatedAt(draft.getUpdatedAt())
                .committedAt(draft.getCommittedAt())
                .build();
        return dto;
    }

    public DraftListItemDTO toListItem(ProductDraft draft) {
        if (draft == null) return null;
        Map<String, Object> meta = new HashMap<>();
        if (draft.getMetaJson() != null) {
            try {
                meta = mapper.readValue(draft.getMetaJson(), Map.class);
            } catch (Exception ignore) { }
        }
        return DraftListItemDTO.builder()
                .id(draft.getId())
                .ownerId(draft.getOwnerId())
                .status(draft.getStatus() != null ? draft.getStatus().name() : null)
                .step(draft.getStep())
                .meta(meta)
                .version(draft.getVersion())
                .updatedAt(draft.getUpdatedAt())
                .build();
    }
}