package com.halaq.backend.payment.criteria;

import com.halaq.backend.shared.PaymentMethod;
import com.halaq.backend.shared.PaymentStatus;
import com.halaq.backend.core.criteria.BaseCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCriteria extends BaseCriteria {
    private Long bookingId;
    private PaymentStatus status;
    private PaymentMethod method;
}