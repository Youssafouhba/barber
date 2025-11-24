package com.halaq.backend.core.specification;

import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.entity.BaseEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSpecification<Criteria extends BaseCriteria, T extends BaseEntity> extends SpecificationHelper<Criteria, T> implements Specification<T> {

    public AbstractSpecification(Criteria criteria) {
        this.criteria = criteria;
    }

    public AbstractSpecification(Criteria criteria, boolean distinct) {
        this.criteria = criteria;
        this.distinct = distinct;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        this.root = root;
        this.query = query;
        this.builder = builder;
        this.predicates = new ArrayList<>();

        if (criteria != null) {
            constructPredicates();
            addOrderAndFilter();
        }
        return getResult();
    }

    /**
     * Cette méthode utilise la réflexion pour construire automatiquement les prédicats.
     * Elle appelle maintenant les méthodes correctes de SpecificationHelper.
     */
    public void constructPredicates() {
        for (Field field : getAllFields(criteria.getClass())) {
            // ✅ CORRECTION : Vérifier la présence de notre nouvelle annotation IgnoreSpecification
            if (field.isAnnotationPresent(IgnoreSpecification.class)) {
                continue; // Ignorer ce champ et passer au suivant
            }
            try {
                field.setAccessible(true);
                Object value = field.get(criteria);
                String fieldName = field.getName();

                // Ignorer si la valeur est nulle ou une chaîne vide
                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                    continue;
                }

                // Heuristique pour déterminer quelle méthode helper appeler
                if (fieldName.toLowerCase().endsWith("like")) {
                    String realFieldName = fieldName.substring(0, fieldName.length() - 4);
                    addPredicate(realFieldName, null, value.toString());
                } else if (fieldName.endsWith("Min")) {
                    handleMinMax(field, fieldName, value);
                } else if (fieldName.endsWith("Max")) {
                    // Ignorer les champs "Max" car ils sont traités avec les champs "Min"
                    continue;
                } else if (fieldName.endsWith("Id")) {
                    String realFieldName = fieldName.substring(0, fieldName.length() - 2);
                    addPredicateFk(realFieldName, "id", (Long) value);
                } else if (value instanceof Boolean) {
                    addPredicateBool(fieldName, (Boolean) value);
                } else if (value instanceof Long) {
                    addPredicate(fieldName, (Long) value);
                } else if (value instanceof String) {
                    addPredicate(fieldName, (String) value);
                } else if (value instanceof LocalDateTime) {
                    addPredicate(fieldName, (LocalDateTime) value);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour gérer les paires Min/Max
    private void handleMinMax(Field minField, String minFieldName, Object minValue) throws Exception {
        String realFieldName = minFieldName.substring(0, minFieldName.length() - 3);
        String maxFieldName = realFieldName + "Max";
        Object maxValue = null;

        // Tenter de trouver le champ "Max" correspondant
        try {
            Field maxField = criteria.getClass().getDeclaredField(maxFieldName);
            maxField.setAccessible(true);
            maxValue = maxField.get(criteria);
        } catch (NoSuchFieldException e) {
            // C'est normal si le champ Max n'existe pas ou n'est pas utilisé
        }

        // Appeler la méthode helper appropriée en fonction du type
        if (minValue instanceof BigDecimal || maxValue instanceof BigDecimal) {
            String minStr = minValue != null ? minValue.toString() : null;
            String maxStr = maxValue != null ? maxValue.toString() : null;
            addPredicateBigDecimal(realFieldName, minStr, maxStr);
        } else if (minValue instanceof Long || maxValue instanceof Long) {
            String minStr = minValue != null ? minValue.toString() : null;
            String maxStr = maxValue != null ? maxValue.toString() : null;
            addPredicateLong(realFieldName, minStr, maxStr);
        } else if (minValue instanceof Integer || maxValue instanceof Integer) {
            String minStr = minValue != null ? minValue.toString() : null;
            String maxStr = maxValue != null ? maxValue.toString() : null;
            addPredicateInt(realFieldName, minStr, maxStr);
        } else if (minValue instanceof Double || maxValue instanceof Double) {
            String minStr = minValue != null ? minValue.toString() : null;
            String maxStr = maxValue != null ? maxValue.toString() : null;
            addPredicateDouble(realFieldName, minStr, maxStr);
        } else if (minValue instanceof LocalDateTime || maxValue instanceof LocalDateTime) {
            addPredicate(realFieldName, (LocalDateTime) minValue, (LocalDateTime) maxValue);
        }
    }

    // Méthode utilitaire pour récupérer tous les champs (y compris hérités)
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }
        return fields;
    }
}