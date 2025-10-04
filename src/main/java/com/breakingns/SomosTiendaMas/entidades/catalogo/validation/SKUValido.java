package com.breakingns.SomosTiendaMas.entidades.catalogo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SKUValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SKUValido {
    String message() default "SKU inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Regex simple por defecto: letras, números, '-', '_' de 3 a 32
    String regex() default "^[A-Za-z0-9_-]{3,32}$";
}
