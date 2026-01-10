package com.halaq.backend.service.criteria;

import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TimeBlockCriteria extends BaseCriteria {
    private Long barberId;
    private LocalDateTime startFrom;
    private LocalDateTime startTo;
    private LocalDateTime endFrom;
    private LocalDateTime endTo;
}


