package com.halaq.backend.user.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceZoneCriteria extends BaseCriteria {
    private Long barberId;
    private String placeId;
    private String name;
}


