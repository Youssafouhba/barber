package com.halaq.backend.core.mapper.context;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Un contexte de mapping pour gérer les relations circulaires dans MapStruct.
 * Il garde une trace des instances déjà mappées pour éviter les boucles infinies.
 */
public class CycleAvoidingMappingContext {
    private final Map<Object, Object> knownInstances = new IdentityHashMap<>();
    private boolean simpleMode = false;
    private final Set<Class<?>> simpleTypes = new HashSet<>();
    public CycleAvoidingMappingContext() {
    }
    /**
     * Retourne VRAI si on doit mapper les champs complexes de cette classe.
     * C'est l'inverse de isSimple.
     */
    public boolean isTypeComplex(Class<?> type) {
        return !isSimple(type);
    }

    public CycleAvoidingMappingContext addSimpleType(Class<?> clazz) {
        this.simpleTypes.add(clazz);
        return this;
    }
    /**
     * Logique intelligente :
     * 1. Si le mode global est activé -> return true
     * 2. Si la classe actuelle est dans la liste des "simples" -> return true
     */
    public boolean isSimple(Class<?> sourceType) {
        return isSimpleMode() || simpleTypes.contains(sourceType);
    }

    public boolean isNotSimple(Object source) {
        if (source == null) return false;
        return !isSimple(source.getClass());
    }
    // Constructeur pour activer le mode simple
    public CycleAvoidingMappingContext(boolean simpleMode) {
        this.simpleMode = simpleMode;
    }

    public boolean isSimpleMode() {
        return simpleMode;
    }

    public void setSimpleMode(boolean simpleMode) {
        this.simpleMode = simpleMode;
    }


    @BeforeMapping
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        return targetType.cast(knownInstances.get(source));
    }

    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target) {
        knownInstances.put(source, target);
    }
}