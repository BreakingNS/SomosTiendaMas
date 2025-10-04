package com.breakingns.SomosTiendaMas.entidades.catalogo.validation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueSlugValidator implements ConstraintValidator<UniqueSlug, String> {

    @PersistenceContext
    private EntityManager em;

    private Class<?> entity;
    private String field;

    @Override
    public void initialize(UniqueSlug constraintAnnotation) {
        this.entity = constraintAnnotation.entity();
        this.field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        String jpql = "select count(e) from " + entity.getSimpleName() + " e " +
                " where e." + field + " = :val and e.deletedAt is null";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("val", value)
                .getSingleResult();
        return count == 0;
    }
}
