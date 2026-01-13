package com.breakingns.SomosTiendaMas.entidades.catalogo.service;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftData;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductDraft;

import java.util.List;

public interface IProductDraftService {

    ProductDraft createDraft(String ownerId);

    ProductDraft getDraft(Long id);

    List<ProductDraft> listByOwner(String ownerId);

    List<ProductDraft> listAll();

    ProductDraft patchDraft(Long id, DraftData partial, Long expectedVersion);

    void deleteDraft(Long id);

    ProductDraft commitDraft(Long id);
}