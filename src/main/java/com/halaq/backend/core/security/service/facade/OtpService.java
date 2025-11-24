package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.controller.dto.request.OtpRequest;
import com.halaq.backend.core.security.controller.dto.request.OtpVerificationRequest;
import com.halaq.backend.core.security.controller.dto.response.AuthResponse;
import com.halaq.backend.core.security.controller.dto.response.OtpResponse;
import com.halaq.backend.core.security.entity.User;

import java.util.List;

public interface OtpService {

    OtpResponse sendOtp(OtpRequest request);

    AuthResponse verifyOtp(OtpVerificationRequest request);

    User createOtpUser(OtpVerificationRequest request);

    void assignUserRole(User user, String userType);

    UserInfo mapToUserInfo(User user);

    String getUserType(User user);

    boolean isProfileCompleted(User user);

    List<String> getUserRoles(User user);

    List<String> getRequiredOnboardingSteps(User user);

    String generateOtpCode();

    String normalizePhoneNumber(String phone);

    String maskPhoneNumber(String phone);
}
