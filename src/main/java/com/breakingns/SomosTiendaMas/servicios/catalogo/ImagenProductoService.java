/*
package com.breakingns.SomosTiendaMas.servicios.catalogo;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.VarianteProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ImagenProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteProductoRepository;
import com.breakingns.SomosTiendaMas.servicios.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional
public class ImagenProductoService {

    private final ImagenProductoRepository imagenRepo;
    private final ProductoRepository productoRepo;
    private final VarianteProductoRepository varianteRepo;
    private final FileStorageService storage;

    public ImagenProductoService(ImagenProductoRepository imagenRepo,
                                 ProductoRepository productoRepo,
                                 VarianteProductoRepository varianteRepo,
                                 FileStorageService storage) {
        this.imagenRepo = imagenRepo;
        this.productoRepo = productoRepo;
        this.varianteRepo = varianteRepo;
        this.storage = storage;
    }

    public ImagenVarianteDTO uploadForProducto(Long productoId, MultipartFile file, String alt, Integer orden) {
        Producto prod = productoRepo.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Producto no encontrado"));
        String url = save(file, "productos/" + productoId);
        ImagenProducto img = new ImagenProducto();
        img.setProducto(prod);
        img.setUrl(url);
        img.setAlt(alt);
        img.setOrden(resolveNextOrden(imagenRepo.findByProductoIdOrderByOrdenAsc(productoId), orden));
        img = imagenRepo.save(img);
        return toDto(img);
    }

    public ImagenVarianteDTO uploadForVariante(Long varianteId, MultipartFile file, String alt, Integer orden) {
        VarianteProducto var = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Variante no encontrada"));
        String url = save(file, "variantes/" + varianteId);
        ImagenProducto img = new ImagenProducto();
        img.setVariante(var);
        img.setUrl(url);
        img.setAlt(alt);
        img.setOrden(resolveNextOrden(imagenRepo.findByVarianteIdOrderByOrdenAsc(varianteId), orden));
        img = imagenRepo.save(img);
        return toDto(img);
    }

    public List<ImagenVarianteDTO> listByProducto(Long productoId) {
        return imagenRepo.findByProductoIdOrderByOrdenAsc(productoId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ImagenVarianteDTO> listByVariante(Long varianteId) {
        return imagenRepo.findByVarianteIdOrderByOrdenAsc(varianteId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public void deleteImagen(Long imagenId) {
        ImagenProducto img = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Imagen no encontrada"));
        storage.deleteByUrl(img.getUrl());
        imagenRepo.delete(img);
    }

    public ImagenVarianteDTO updateOrden(Long imagenId, Integer orden) {
        if (orden == null) throw new ResponseStatusException(BAD_REQUEST, "Orden requerido");
        ImagenProducto img = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Imagen no encontrada"));
        img.setOrden(orden);
        return toDto(imagenRepo.save(img));
    }

    public ImagenVarianteDTO updateAlt(Long imagenId, String alt) {
        ImagenProducto img = imagenRepo.findById(imagenId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Imagen no encontrada"));
        img.setAlt(alt);
        return toDto(imagenRepo.save(img));
    }

    private String save(MultipartFile file, String subfolder) {
        try {
            return storage.saveImage(file, subfolder);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No se pudo guardar la imagen");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, ex.getMessage());
        }
    }

    private int resolveNextOrden(List<ImagenProducto> existentes, Integer pedido) {
        if (pedido != null) return pedido;
        return existentes.stream()
                .map(ImagenProducto::getOrden)
                .filter(o -> o != null)
                .max(Comparator.naturalOrder())
                .map(max -> max + 1)
                .orElse(0);
    }

    private ImagenVarianteDTO toDto(ImagenProducto i) {
        return ImagenVarianteDTO.builder()
                .id(i.getId())
                .productoId(i.getProducto() != null ? i.getProducto().getId() : null)
                .varianteId(i.getVariante() != null ? i.getVariante().getId() : null)
                .url(i.getUrl())
                .alt(i.getAlt())
                .orden(i.getOrden())
                .build();
    }
}
 */