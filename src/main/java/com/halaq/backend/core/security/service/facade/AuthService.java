package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.controller.dto.request.*;
import com.halaq.backend.core.security.controller.dto.response.AuthResponse;
import com.halaq.backend.core.security.controller.dto.response.PasswordResetResponse;
import com.halaq.backend.core.security.controller.dto.response.VerificationResponse;
import com.halaq.backend.core.security.controller.dto.response.VerificationStatusResponse;
import com.halaq.backend.core.security.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {

    AuthResponse login(LoginRequest request) throws Exception;

    String sendWelcomeEmail(String toEmail, String userName);

    AuthResponse register(RegisterRequest request) throws Exception;

    VerificationResponse sendEmailVerification(EmailVerificationRequest request);

    VerificationResponse verifyEmail(EmailVerificationCodeRequest request);

    VerificationResponse resendEmailVerification(String email);

    VerificationResponse resendPhoneVerification(String email);

    // OPTIMIZED: Load user with all associations in single query
    @Transactional(readOnly = true)
    User findUserByLoginWithAllData(String login);

    AuthResponse refreshToken(RefreshTokenRequest request);

    LogoutResponse logout(LogoutRequest request);

    PasswordResetResponse forgotPassword(ForgotPasswordRequest request);

    PasswordResetResponse validateResetCode(ValidateResetCodeRequest request);

    PasswordResetResponse resetPassword(ResetPasswordRequest request);

    User assignUserRole(User user, String userType);

    VerificationStatusResponse getVerificationStatus();

    UserInfo getUserInfo();
}
