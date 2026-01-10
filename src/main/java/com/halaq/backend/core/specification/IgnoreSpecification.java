package com.halaq.backend.core.specification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field in a Criteria class that should be ignored
 * by the automatic predicate construction in AbstractSpecification.
 */
@Retention(RetentionPolicy.RUNTIME) // L'annotation doit être disponible à l'exécution pour la réflexion
@Target(ElementType.FIELD) // L'annotation ne peut être placée que sur des champs
public @interface IgnoreSpecification {
}