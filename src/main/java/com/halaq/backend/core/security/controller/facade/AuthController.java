package com.halaq.backend.core.security.controller.facade;

import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.controller.dto.request.*;
import com.halaq.backend.core.security.controller.dto.request.LogoutResponse;
import com.halaq.backend.core.security.controller.dto.response.*;
import com.halaq.backend.core.security.service.facade.AuthService;
import com.halaq.backend.core.security.service.facade.OtpService;
import com.halaq.backend.core.security.service.facade.SocialAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Authentication", description = "Authentication endpoints including social login and OTP")
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    private final OtpService otpService;

    private final SocialAuthService socialAuthService;

    public AuthController(AuthService authService, OtpService otpService, SocialAuthService socialAuthService) {
        this.authService = authService;
        this.otpService = otpService;
        this.socialAuthService = socialAuthService;
    }


    /**
     * get authenticated user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getUserInfo() {
        return ResponseEntity.ok(authService.getUserInfo());
    }


    /**
     * Obtenir l'état actuel de vérification du compte
     * Frontend appelle ça pour savoir où l'utilisateur en est
     */
    @GetMapping("/verification-status")
    public ResponseEntity<VerificationStatusResponse> getVerificationStatus() {
        return ResponseEntity.ok(authService.getVerificationStatus());
    }

    @Operation(summary = "Send welcome email")
    @PostMapping("/sendWelcomeEmail")
    public ResponseEntity<OtpResponse> sendWelcomeEmail(@RequestBody String toEmail, @RequestParam("userName") String userName) {
        try {
            OtpResponse response = otpService.sendOtp(OtpRequest.builder()
                    .phone(toEmail)
                    .build());
            return ResponseEntity.ok(OtpResponse.builder()
                    .success(true)
                    .message(response.getMessage())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    OtpResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }


    @Operation(summary = "Login with email/username and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Register new user account")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Send OTP to phone number")
    @PostMapping("/otp/send")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        try {
            OtpResponse response = otpService.sendOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    OtpResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Verify OTP and login/register")
    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            AuthResponse response = otpService.verifyOtp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Google OAuth login")
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            AuthResponse response = socialAuthService.googleLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Facebook OAuth login")
    @PostMapping("/facebook")
    public ResponseEntity<AuthResponse> facebookLogin(@Valid @RequestBody FacebookAuthRequest request) {
        try {
            AuthResponse response = socialAuthService.facebookLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Apple OAuth login")
    @PostMapping("/apple")
    public ResponseEntity<AuthResponse> appleLogin(@Valid @RequestBody AppleAuthRequest request) {
        try {
            AuthResponse response = socialAuthService.appleLogin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Send email verification")
    @PostMapping("/verify/email/send")
    public ResponseEntity<VerificationResponse> sendEmailVerification(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            VerificationResponse response = authService.sendEmailVerification(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    VerificationResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Verify email with code")
    @PostMapping("/verify/email")
    public ResponseEntity<VerificationResponse> verifyEmail(@Valid @RequestBody EmailVerificationCodeRequest request) {
        try {
            VerificationResponse response = authService.verifyEmail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    VerificationResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Refresh JWT token")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            LogoutResponse response = authService.logout(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    LogoutResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            PasswordResetResponse response = authService.forgotPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    PasswordResetResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }


    @Operation(summary = "Validate reset password code")
    @PostMapping("/validate-reset-code")
    public ResponseEntity<PasswordResetResponse> validateResetCode(@Valid @RequestBody ValidateResetCodeRequest request) {
        try {
            PasswordResetResponse response = authService.validateResetCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    PasswordResetResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(summary = "Reset password with code")
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            PasswordResetResponse response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    PasswordResetResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
