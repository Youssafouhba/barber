package com.halaq.backend.payment.service.facade;

import com.halaq.backend.payment.dto.PaymentIntentDTO;
import com.halaq.backend.payment.dto.PaymentResultDTO;
import com.halaq.backend.payment.entity.Payment;
import com.halaq.backend.payment.criteria.PaymentCriteria;
import com.halaq.backend.shared.PaymentMethod;
import com.halaq.backend.core.service.IService;

import java.math.BigDecimal;

public interface PaymentService extends IService<Payment, PaymentCriteria> {
    Payment processPaymentForBooking(Long bookingId);
    
    PaymentIntentDTO rechargeWallet(Long userId, BigDecimal amount, String paymentMethodId);
    
    void handleStripeWebhook(String payload, String signature);
    
    PaymentResultDTO processReservationPayment(
        Long bookingId,
        Long clientId,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod);
}