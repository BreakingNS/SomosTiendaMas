package com.breakingns.SomosTiendaMas.entidades.catalogo.mapper;

import com.breakingns.SomosTiendaMas.entidades.catalogo.dto.shared.PagedResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PagedResponseUtil {

    private PagedResponseUtil() {}

    /**
     * Convierte un Page<T> de Spring a PagedResponseDTO<R> usando el mapper provisto.
     */
    public static <T, R> PagedResponseDTO<R> fromPage(Page<T> page, Function<? super T, ? extends R> mapper) {
        PagedResponseDTO<R> out = new PagedResponseDTO<>();
        if (page == null) {
            out.setItems(List.of());
            out.setTotal(0);
            return out;
        }
        List<R> items = (List<R>) page.map(mapper).getContent();
        out.setItems(items);
        out.setTotal(page.getTotalElements());
        return out;
    }

    /**
     * Convierte una lista a PagedResponseDTO (total = tamaño de la lista).
     */
    public static <T, R> PagedResponseDTO<R> fromList(List<T> list, Function<? super T, ? extends R> mapper) {
        PagedResponseDTO<R> out = new PagedResponseDTO<>();
        if (list == null || list.isEmpty()) {
            out.setItems(List.of());
            out.setTotal(0);
            return out;
        }
        out.setItems(list.stream().map(mapper).collect(Collectors.toList()));
        out.setTotal(list.size());
        return out;
    }
}