package com.halaq.backend.core.service;

import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.core.service.AbstractServiceImpl;

public abstract class AbstractReferentielServiceImpl<T extends BaseEntity, CRITERIA extends BaseCriteria, REPO extends AbstractRepository<T, Long>> extends AbstractServiceImpl<T, CRITERIA, REPO> {

    public AbstractReferentielServiceImpl(REPO dao) {
        super(dao);
    }

}
