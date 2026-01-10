package com.halaq.backend.service.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCriteria extends BaseCriteria {
    private Long authorId;
    private Long barberId;
    private Long bookingId;
    private Integer ratingMin;
    private Integer ratingMax;
}