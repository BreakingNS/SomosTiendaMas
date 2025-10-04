package com.breakingns.SomosTiendaMas.entidades.catalogo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SKUValidoValidator implements ConstraintValidator<SKUValido, String> {

    private Pattern pattern;

    @Override
    public void initialize(SKUValido constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.regex());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // usar @NotNull si es requerido
        return pattern.matcher(value).matches();
    }
}
