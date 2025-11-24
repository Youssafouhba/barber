package com.halaq.backend.user.service.impl;

import com.halaq.backend.user.entity.Favorite;
import com.halaq.backend.user.criteria.FavoriteCriteria;
import com.halaq.backend.user.repository.FavoriteRepository;
import com.halaq.backend.user.service.facade.FavoriteService;
import com.halaq.backend.user.specification.FavoriteSpecification;
import com.halaq.backend.core.exception.BusinessRuleException;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteServiceImpl extends AbstractServiceImpl<Favorite, FavoriteCriteria, FavoriteRepository> implements FavoriteService {

    public FavoriteServiceImpl(FavoriteRepository dao) {
        super(dao);
    }

    @Override
    @Transactional
    public Favorite create(Favorite favorite) {
        // Business logic: Prevent duplicate favorites
        FavoriteCriteria criteria = new FavoriteCriteria();
        criteria.setClientId(favorite.getClient().getId());
        criteria.setBarberId(favorite.getBarber().getId());
        if (!findByCriteria(criteria).isEmpty()) {
            throw new BusinessRuleException("This barber is already in the client's favorites.");
        }
        return super.create(favorite);
    }

    @Override
    public void configure() {
        super.configure(Favorite.class, FavoriteSpecification.class);
    }
}