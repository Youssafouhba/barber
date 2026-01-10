package com.halaq.backend.user.specification;

import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.specification.Operator;
import com.halaq.backend.user.criteria.ServiceZoneCriteria;
import com.halaq.backend.user.entity.ServiceZone;

public class ServiceZoneSpecification extends AbstractSpecification<ServiceZoneCriteria, ServiceZone> {

    public ServiceZoneSpecification(ServiceZoneCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("barber", "id", criteria.getBarberId());
        addPredicate("placeId", criteria.getPlaceId(), Operator.EQUALS.toString());
        addPredicate("name", criteria.getName());
    }
}


