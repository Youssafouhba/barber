package com.halaq.backend.service.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class AvailabilityCriteria extends BaseCriteria {
    private Long barberId;
    private DayOfWeek day;
}