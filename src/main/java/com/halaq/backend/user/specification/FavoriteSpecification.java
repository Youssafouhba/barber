package com.halaq.backend.user.specification;

import com.halaq.backend.user.entity.Favorite;
import com.halaq.backend.user.criteria.FavoriteCriteria;
import com.halaq.backend.core.specification.AbstractSpecification;

public class FavoriteSpecification extends AbstractSpecification<FavoriteCriteria, Favorite> {

    public FavoriteSpecification(FavoriteCriteria criteria) {
        super(criteria);
    }

    @Override
    public void constructPredicates() {
        addPredicateFk("client", "id", criteria.getClientId());
        addPredicateFk("barber", "id", criteria.getBarberId());
    }
}