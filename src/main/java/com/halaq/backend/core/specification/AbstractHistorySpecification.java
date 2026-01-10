package com.halaq.backend.core.specification;

import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.history.HistCriteria;
import com.halaq.backend.core.specification.SpecificationHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHistorySpecification<Criteria extends HistCriteria, H extends BaseEntity> extends SpecificationHelper<Criteria, H> implements Specification<H> {

    public AbstractHistorySpecification(Criteria criteria) {
        this.criteria = criteria;
    }

    public AbstractHistorySpecification(Criteria criteria, boolean distinct) {
        this.criteria = criteria;
        this.distinct = distinct;
    }

    public Predicate toPredicate(Root<H> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> myPredicates = new ArrayList<>();
        attachSearchElement(root, query, builder, myPredicates);
        if (criteria != null) {
            addPredicateId("id", criteria);
            addPredicate("objectName", criteria.getObjectName(), criteria.getObjectNameLike());
            addPredicate("data", criteria.getData(), criteria.getDataLike());
            addPredicate("username", criteria.getUsername(), criteria.getUsernameLike());
            addPredicateLong("userId", criteria.getUserId());
            addPredicate("actionType", criteria.getActionTypeLike(), criteria.getActionTypeLike());
            addPredicateLong("objectId", criteria.getObjectId());
            addPredicate("actionType", criteria.getDate(), criteria.getDateFrom(), criteria.getDateTo());
            addOrderAndFilter();
        }
        return getResult();
    }
}
