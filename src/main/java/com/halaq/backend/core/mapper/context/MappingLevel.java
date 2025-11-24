package com.halaq.backend.core.mapper.context;

public enum MappingLevel {
    SIMPLE,     // uniquement les champs simples
    PARTIAL,    // quelques objets
    FULL        // tout mapper (services, reviews, availability...)
}
