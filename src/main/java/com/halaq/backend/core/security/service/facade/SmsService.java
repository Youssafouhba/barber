package com.halaq.backend.core.security.service.facade;

public interface SmsService {
    void sendSms(String phoneNumber, String message);

    void sendViaTwilio(String phoneNumber, String message);

    void sendViaNexmo(String phoneNumber, String message);

    void sendViaLocalProvider(String phoneNumber, String message);

    void sendViaMock(String phoneNumber, String message);

    boolean isValidMoroccanPhoneNumber(String phoneNumber);

    String formatPhoneNumber(String phoneNumber);
}
