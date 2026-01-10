package com.halaq.backend.user.repository;

import com.halaq.backend.user.entity.Favorite;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends AbstractRepository<Favorite, Long> {
}