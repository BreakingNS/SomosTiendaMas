package com.breakingns.SomosTiendaMas.entidades.catalogo.model;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftData;
import com.breakingns.SomosTiendaMas.entidades.catalogo.enums.DraftStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_draft", indexes = {
        @Index(name = "idx_product_draft_owner", columnList = "owner_id"),
        @Index(name = "idx_product_draft_status", columnList = "status"),
        @Index(name = "idx_product_draft_updated", columnList = "updated_at")
})
@Getter
@Setter
public class ProductDraft extends BaseEntidadAuditada {

    @Column(name = "owner_id", length = 128, nullable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private DraftStatus status = DraftStatus.DRAFT;

    @Column(name = "step")
    private Integer step = 1;

    // JSON blob that contains all draft data (category, price, attributes, etc.)
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    // JSON array with image keys/paths uploaded for this draft (redundant but handy)
    @Column(name = "images_json", columnDefinition = "TEXT")
    private String imagesJson;

    // small meta JSON for UI (title, price preview)
    @Column(name = "meta_json", columnDefinition = "TEXT")
    private String metaJson;

    // optimistic locking
    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "committed_at")
    private LocalDateTime committedAt;

    @Transient
    private DraftData draftData;

    private static final ObjectMapper DRAFT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public DraftData getDraftData() {
        if (this.draftData == null && this.dataJson != null) {
            try {
                this.draftData = DRAFT_MAPPER.readValue(this.dataJson, DraftData.class);
            } catch (IOException e) {
                this.draftData = null;
            }
        }
        return this.draftData;
    }

    public void setDraftData(DraftData draftData) {
        this.draftData = draftData;
        try {
            this.dataJson = draftData == null ? null : DRAFT_MAPPER.writeValueAsString(draftData);
        } catch (IOException e) {
            // leave dataJson unchanged on serialization error
        }
    }

}
