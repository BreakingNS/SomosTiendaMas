package com.breakingns.SomosTiendaMas.entidades.catalogo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueSlugValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueSlug {
    String message() default "Slug ya existe";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Entidad JPA donde validar, ejemplo: Producto.class
    Class<?> entity();

    // Nombre del campo slug en la entidad (por defecto 'slug')
    String field() default "slug";
}
