package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.producto.ImagenProductoDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenProducto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Producto;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ImagenProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ProductoRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenProductoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ImagenProductoService implements IImagenProductoService {

    private final ImagenProductoRepository imagenRepo;
    private final ProductoRepository productoRepo;

    public ImagenProductoService(ImagenProductoRepository imagenRepo, ProductoRepository productoRepo) {
        this.imagenRepo = imagenRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public ImagenProductoDTO crear(ImagenProductoDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        ImagenProducto e = new ImagenProducto();

        if (dto.getProductoId() != null) {
            Producto p = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + dto.getProductoId()));
            e.setProducto(p);
        }

        // campos comunes — adaptar nombres si tu DTO/entidad difieren
        e.setUrl(dto.getUrl());
        e.setAlt(dto.getAlt());

        // calcular orden: siguiente disponible
        List<ImagenProducto> actuales = imagenRepo.findByProductoIdOrderByOrdenAsc(dto.getProductoId());
        int nextOrden = 0;
        if (actuales != null && !actuales.isEmpty()) {
            ImagenProducto last = actuales.get(actuales.size() - 1);
            nextOrden = (last.getOrden() == null ? 0 : last.getOrden()) + 1;
        }
        e.setOrden(nextOrden);

        ImagenProducto saved = imagenRepo.save(e);
        return toDto(saved);
    }

    @Override
    public ImagenProductoDTO actualizar(Long id, ImagenProductoDTO dto) {
        ImagenProducto e = imagenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + id));
        if (dto.getUrl() != null) e.setUrl(dto.getUrl());
        if (dto.getAlt() != null) e.setAlt(dto.getAlt());
        if (dto.getOrden() != null) e.setOrden(dto.getOrden());
        // no cambiamos productoId aquí; si se necesita, manejarlo explícitamente
        ImagenProducto updated = imagenRepo.save(e);
        return toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenProductoDTO obtenerPorId(Long id) {
        ImagenProducto e = imagenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + id));
        return toDto(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenProductoDTO> listarPorProductoId(Long productoId) {
        List<ImagenProducto> list = imagenRepo.findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        List<ImagenProductoDTO> out = new ArrayList<>();
        if (list == null) return out;
        for (ImagenProducto e : list) out.add(toDto(e));
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenProductoDTO obtenerPrimeraPorProductoId(Long productoId) {
        Optional<ImagenProducto> opt = imagenRepo.findFirstByProductoIdOrderByOrdenAsc(productoId);
        return opt.map(this::toDto).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        ImagenProducto e = imagenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + id));
        e.setDeletedAt(LocalDateTime.now());
        imagenRepo.save(e);
    }

    @Override
    public void eliminarPorProductoId(Long productoId) {
        List<ImagenProducto> list = imagenRepo.findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        if (list == null || list.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        for (ImagenProducto e : list) {
            e.setDeletedAt(now);
        }
        imagenRepo.saveAll(list);
    }

    @Override
    public void reordenarPorProducto(Long productoId, List<Long> imagenIdsOrdenados) {
        if (imagenIdsOrdenados == null || imagenIdsOrdenados.isEmpty()) return;
        List<ImagenProducto> actuales = imagenRepo.findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(productoId);
        // Mapear ids -> entidad rápida búsqueda
        java.util.Map<Long, ImagenProducto> map = new java.util.HashMap<>();
        for (ImagenProducto e : actuales) map.put(e.getId(), e);

        int orden = 0;
        List<ImagenProducto> toSave = new ArrayList<>();
        for (Long id : imagenIdsOrdenados) {
            ImagenProducto e = map.get(id);
            if (e == null) continue; // ignorar ids que no pertenecen
            e.setOrden(orden++);
            toSave.add(e);
        }
        if (!toSave.isEmpty()) imagenRepo.saveAll(toSave);
    }

    // mapeo simple entidad -> DTO (adaptar si los nombres de campos difieren)
    private ImagenProductoDTO toDto(ImagenProducto e) {
        if (e == null) return null;
        ImagenProductoDTO dto = new ImagenProductoDTO();
        dto.setId(e.getId());
        dto.setProductoId(e.getProducto() != null ? e.getProducto().getId() : null);
        dto.setUrl(e.getUrl());
        dto.setAlt(e.getAlt());
        dto.setOrden(e.getOrden());
        // No estan implementados
        /*
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setDeletedAt(e.getDeletedAt());
        */
        return dto;
    }

    @Override
    public List<ImagenProductoDTO> uploadAndCreate(Long productoId, MultipartFile[] files) {
        List<ImagenProductoDTO> out = new ArrayList<>();
        if (files == null || files.length == 0) return out;

        // carpeta base local (puedes cambiar por propiedad)
        Path baseDir = Path.of("uploads", "productos", String.valueOf(productoId));
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear directorio de uploads", e);
        }

        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;
            String original = mf.getOriginalFilename() == null ? "file" : Path.of(mf.getOriginalFilename()).getFileName().toString();
            String safeName = System.currentTimeMillis() + "-" + original.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
            Path target = baseDir.resolve(safeName);
            try (InputStream in = mf.getInputStream()) {
                Files.copy(in, target);
            } catch (IOException ex) {
                // seguir con siguientes archivos si uno falla (o lanzar según prefieras)
                continue;
            }

            // URL pública relativa (ajustar si servís estáticos desde otra ruta / CDN)
            String publicUrl = "/uploads/productos/" + productoId + "/" + safeName;

            ImagenProductoDTO dto = new ImagenProductoDTO();
            dto.setProductoId(productoId);
            dto.setUrl(publicUrl);
            dto.setAlt(original);

            ImagenProductoDTO created = crear(dto);
            out.add(created);
        }

        return out;
    }
}