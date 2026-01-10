package com.halaq.backend.service.criteria;

import com.halaq.backend.shared.BookingStatus;
import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingCriteria extends BaseCriteria {
    private Long clientId;
    private Long barberId;
    private BookingStatus status;
    private LocalDateTime scheduledAtFrom;
    private LocalDateTime scheduledAtTo;
}