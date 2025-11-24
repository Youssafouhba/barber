package com.halaq.backend.user.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteCriteria extends BaseCriteria {
    private Long clientId;
    private Long barberId;
}