package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.draft.DraftData;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ProductDraft;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductDraftRepository;
import com.breakingns.SomosTiendaMas.security.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IProductDraftService;

import jakarta.persistence.OptimisticLockException;
import java.util.List;
import java.util.Map;

@Service
@Transactional

public class ProductDraftService implements IProductDraftService {

    private final ProductDraftRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductDraftService(ProductDraftRepository repo) {
        this.repo = repo;
    }

    public ProductDraft createDraft(String ownerId) {
        ProductDraft d = new ProductDraft();
        d.setOwnerId(ownerId);
        d.setStatus(com.breakingns.SomosTiendaMas.entidades.catalogo.enums.DraftStatus.DRAFT);
        d.setStep(1);
        return repo.save(d);
    }

    @Transactional(readOnly = true)
    public ProductDraft getDraft(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Draft no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ProductDraft> listByOwner(String ownerId) {
        return repo.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<ProductDraft> listAll() {
        return repo.findAll();
    }

    public ProductDraft patchDraft(Long id, DraftData partial, Long expectedVersion) {
        ProductDraft d = repo.findById(id).orElseThrow(() -> new NotFoundException("Draft no encontrado"));
        if (expectedVersion != null && d.getVersion() != null && !expectedVersion.equals(d.getVersion())) {
            throw new OptimisticLockException("Draft version mismatch");
        }

        DraftData current = d.getDraftData();
        if (current == null) current = new DraftData();

        if (partial.getProducto() != null) current.setProducto(partial.getProducto());
        if (partial.getOpcion() != null) current.setOpcion(partial.getOpcion());
        if (partial.getImagenes() != null) current.setImagenes(partial.getImagenes());
        if (partial.getPrecio() != null) current.setPrecio(partial.getPrecio());
        if (partial.getInventario() != null) current.setInventario(partial.getInventario());
        if (partial.getPropiedadesFisicas() != null) current.setPropiedadesFisicas(partial.getPropiedadesFisicas());

        d.setDraftData(current);

        // update metaJson lightly for UI
        try {
            Object metaNombre = current.getProducto() != null ? current.getProducto().getNombre() : null;
            Object metaPrecio = current.getPrecio() != null ? current.getPrecio().getMontoCentavos() : null;
            d.setMetaJson(mapper.writeValueAsString(Map.of("nombre", metaNombre, "precio_centavos", metaPrecio)));
        } catch (Exception ignore) {
        }

        return repo.save(d);
    }

    public void deleteDraft(Long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Draft no encontrado");
        repo.deleteById(id);
    }

    /**
     * Commit draft -> create real product/entities. Stub: implement business logic here.
     */
    public ProductDraft commitDraft(Long id) {
        // TODO: validar, crear entidades finales en transacción, mover imágenes, etc.
        throw new UnsupportedOperationException("commitDraft no implementado aún");
    }
}