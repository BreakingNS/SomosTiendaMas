package com.breakingns.SomosTiendaMas.entidades.catalogo.service.impl;

import com.breakingns.SomosTiendaMas.config.UploadProperties;
import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.imagen.ImagenVarianteDTO;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.ImagenVariante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.model.Variante;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.ImagenVarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.repository.VarianteRepository;
import com.breakingns.SomosTiendaMas.entidades.catalogo.service.IImagenVarianteService;
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
import java.nio.file.Paths;

@Service
@Transactional
public class ImagenVarianteService implements IImagenVarianteService {

    private final ImagenVarianteRepository imagenRepo;
    private final VarianteRepository varianteRepo;
    private final UploadProperties uploadProps;

    public ImagenVarianteService(ImagenVarianteRepository imagenRepo, VarianteRepository varianteRepo, UploadProperties uploadProps) {
        this.imagenRepo = imagenRepo;
        this.varianteRepo = varianteRepo;
        this.uploadProps = uploadProps;
    }

    @Override
    public ImagenVarianteDTO crear(ImagenVarianteDTO dto) {
        if (dto == null) throw new IllegalArgumentException("dto es null");
        ImagenVariante e = new ImagenVariante();

        if (dto.getVarianteId() != null) {
            Variante p = varianteRepo.findById(dto.getVarianteId())
                    .orElseThrow(() -> new EntityNotFoundException("Variante no encontrado: " + dto.getVarianteId()));
            e.setVariante(p);
        }

        // campos comunes — adaptar nombres si tu DTO/entidad difieren
        e.setUrl(dto.getUrl());
        e.setAlt(dto.getAlt());

        // calcular orden: siguiente disponible
        List<ImagenVariante> actuales = imagenRepo.findByVariante_IdOrderByOrdenAsc(dto.getVarianteId());
        int nextOrden = 0;
        if (actuales != null && !actuales.isEmpty()) {
            ImagenVariante last = actuales.get(actuales.size() - 1);
            nextOrden = (last.getOrden() == null ? 0 : last.getOrden()) + 1;
        }
        e.setOrden(nextOrden);

        ImagenVariante saved = imagenRepo.save(e);
        return toDto(saved);
    }

    @Override
    public ImagenVarianteDTO actualizar(Long id, ImagenVarianteDTO dto) {
        ImagenVariante e = imagenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + id));
        if (dto.getUrl() != null) e.setUrl(dto.getUrl());
        if (dto.getAlt() != null) e.setAlt(dto.getAlt());
        if (dto.getOrden() != null) e.setOrden(dto.getOrden());
        // no cambiamos varianteId aquí; si se necesita, manejarlo explícitamente
        ImagenVariante updated = imagenRepo.save(e);
        return toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenVarianteDTO obtenerPorId(Long id) {
        ImagenVariante e = imagenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + id));
        return toDto(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenVarianteDTO> listarPorVarianteId(Long varianteId) {
        List<ImagenVariante> list = imagenRepo.findByVariante_IdAndDeletedAtIsNullOrderByOrdenAsc(varianteId);
        List<ImagenVarianteDTO> out = new ArrayList<>();
        if (list == null) return out;
        for (ImagenVariante e : list) out.add(toDto(e));
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenVarianteDTO obtenerPrimeraPorVarianteId(Long varianteId) {
        Optional<ImagenVariante> opt = imagenRepo.findFirstByVariante_IdOrderByOrdenAsc(varianteId);
        return opt.map(this::toDto).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        ImagenVariante e = imagenRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada: " + id));
        e.setDeletedAt(LocalDateTime.now());
        imagenRepo.save(e);
    }

    @Override
    public void eliminarPorVarianteId(Long varianteId) {
        List<ImagenVariante> list = imagenRepo.findByVariante_IdAndDeletedAtIsNullOrderByOrdenAsc(varianteId);
        if (list == null || list.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        for (ImagenVariante e : list) {
            e.setDeletedAt(now);
        }
        imagenRepo.saveAll(list);
    }

    @Override
    public void reordenarPorVariante(Long varianteId, List<Long> imagenIdsOrdenados) {
        if (imagenIdsOrdenados == null || imagenIdsOrdenados.isEmpty()) return;
        List<ImagenVariante> actuales = imagenRepo.findByVariante_IdAndDeletedAtIsNullOrderByOrdenAsc(varianteId);
        // Mapear ids -> entidad rápida búsqueda
        java.util.Map<Long, ImagenVariante> map = new java.util.HashMap<>();
        for (ImagenVariante e : actuales) map.put(e.getId(), e);

        int orden = 0;
        List<ImagenVariante> toSave = new ArrayList<>();
        for (Long id : imagenIdsOrdenados) {
            ImagenVariante e = map.get(id);
            if (e == null) continue; // ignorar ids que no pertenecen
            e.setOrden(orden++);
            toSave.add(e);
        }
        if (!toSave.isEmpty()) imagenRepo.saveAll(toSave);
    }

    // mapeo simple entidad -> DTO (adaptar si los nombres de campos difieren)
    private ImagenVarianteDTO toDto(ImagenVariante e) {
        if (e == null) return null;
        ImagenVarianteDTO dto = new ImagenVarianteDTO();
        dto.setId(e.getId());
        dto.setVarianteId(e.getVariante() != null ? e.getVariante().getId() : null);
        // normalizar url: si no empieza con '/' o 'http' añadir prefijo urlBase
        String url = e.getUrl();
        if (url != null && !url.startsWith("/") && !url.startsWith("http")) {
            url = uploadProps.getUrlBase() + "/" + url;
        }
        dto.setUrl(url);
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
    public List<ImagenVarianteDTO> uploadAndCreate(Long varianteId, MultipartFile[] files) {
        List<ImagenVarianteDTO> out = new ArrayList<>();
        if (files == null || files.length == 0) return out;

        // carpeta base según UploadProperties (usa la ruta configurada en UploadProperties)
        Path baseDir = uploadProps.getRoot().resolve(Paths.get("variantes", String.valueOf(varianteId)));

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

            // URL pública relativa usando urlBase de UploadProperties
            String publicUrl = uploadProps.getUrlBase() + "/variantes/" + varianteId + "/" + safeName;


            ImagenVarianteDTO dto = new ImagenVarianteDTO();
            dto.setVarianteId(varianteId);
            dto.setUrl(publicUrl);
            dto.setAlt(original);

            ImagenVarianteDTO created = crear(dto);
            out.add(created);
        }

        return out;
    }
}