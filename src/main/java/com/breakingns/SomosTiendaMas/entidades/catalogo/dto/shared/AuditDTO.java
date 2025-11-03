package com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditDTO {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long version;
}