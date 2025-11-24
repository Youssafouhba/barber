package com.halaq.backend.core.specification;

public enum Operator {
    EQUALS,
    NOT_EQUALS,
    LIKE,
    LIKE_LOWER, // Pour les recherches insensibles à la casse
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL_TO,
    LESS_THAN,
    LESS_THAN_OR_EQUAL_TO,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL
}