package com.breakingns.SomosTiendaMas.entidades.catalogo.controller;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftCreateResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftListItemDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftPatchRequestDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftResponseDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.mapper.ProductDraftMapper;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductDraft;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/preproductos")
@RequiredArgsConstructor
public class ProductDraftController {

    private final IProductDraftService draftService;
    private final ProductDraftMapper mapper;

    @PostMapping
    public ResponseEntity<DraftCreateResponseDTO> create(@RequestParam String ownerId) {
        ProductDraft created = draftService.createDraft(ownerId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toCreateResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DraftResponseDTO> getById(@PathVariable Long id) {
        ProductDraft d = draftService.getDraft(id);
        return ResponseEntity.ok(mapper.toResponse(d));
    }

    @GetMapping
    public ResponseEntity<List<DraftListItemDTO>> listByOwner(@RequestParam(required = false) String ownerId) {
        List<ProductDraft> list;
        if (ownerId == null) {
            list = draftService.listAll();
        } else {
            list = draftService.listByOwner(ownerId);
        }
        List<DraftListItemDTO> dtoList = list.stream().map(mapper::toListItem).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DraftResponseDTO> patch(@PathVariable Long id, @Valid @RequestBody DraftPatchRequestDTO req) {
        ProductDraft updated = draftService.patchDraft(id, req.getData(), req.getExpectedVersion());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        draftService.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/commit")
    public ResponseEntity<DraftResponseDTO> commit(@PathVariable Long id) {
        ProductDraft committed = draftService.commitDraft(id);
        return ResponseEntity.ok(mapper.toResponse(committed));
    }
}
