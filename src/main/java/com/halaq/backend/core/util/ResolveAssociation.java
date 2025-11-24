package com.halaq.backend.core.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field in an entity as a manageable association.
 * The AbstractServiceImpl will use this to automatically find and attach
 * the related entity before saving.
 */
@Target(ElementType.FIELD) // This annotation can only be applied to fields
@Retention(RetentionPolicy.RUNTIME) // It needs to be available at runtime for reflection
public @interface ResolveAssociation {
}
