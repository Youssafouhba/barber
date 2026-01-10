package com.halaq.backend.core.security.service.impl;


import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.controller.dto.request.*;
import com.halaq.backend.core.security.controller.dto.request.LogoutResponse;
import com.halaq.backend.core.security.controller.dto.response.*;
import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.entity.RoleUser;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.enums.AccountStatus;
import com.halaq.backend.core.security.jwt.JwtTokenUtil;
import com.halaq.backend.core.security.service.facade.*;
import com.halaq.backend.core.transverse.emailling.service.EmailService;
import com.halaq.backend.core.util.DateUtil;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Client;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final RoleService roleService;

    private final UserService userService;

    private final JwtTokenUtil jwtTokenUtil;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final RefreshTokenService refreshTokenService;

    private final LoginAttemptService loginAttemptService;

    private final OtpService otpService;

    private final VerificationTrackerService verificationTrackerService;

    public AuthServiceImpl(RoleService roleService, UserService userService, JwtTokenUtil jwtTokenUtil, PasswordEncoder passwordEncoder, EmailService emailService, RefreshTokenService refreshTokenService, LoginAttemptService loginAttemptService, OtpService otpService, VerificationTrackerService verificationTrackerService) {
        this.roleService = roleService;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.otpService = otpService;
        this.verificationTrackerService = verificationTrackerService;
    }


    // OPTIMIZED: Single query with all associations
    @Transactional(readOnly = true)
    @Override
    public AuthResponse login(LoginRequest request) throws Exception {
        // Find user with ALL data in ONE query
        User user = findUserByLoginWithAllData(request.getLogin());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(user.getUsername());
            throw new BadCredentialsException("Invalid credentials");
        }
        loginAttemptService.loginSucceeded(user.getUsername());
        // All data already loaded - no N+1 queries
        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        VerificationTracker tracker = user.getVerificationTracker() != null
                ? user.getVerificationTracker()
                : verificationTrackerService.createTracker(user);

        if(!tracker.getEmailVerified())
            sendEmailVerification( EmailVerificationRequest.builder().email(user.getEmail()).build());
        /*if(!tracker.getPhoneVerified())
            otpService.sendOtp( OtpRequest.builder().phone(user.getPhone()).build());*/
        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenUtil.getJwtExpirationInMs())
                .user(mapToUserInfo(user, tracker))
                .build();
    }

    // OPTIMIZED: Load user with all associations in single query
    @Transactional(readOnly = true)
    @Override
    public User findUserByLoginWithAllData(String login) {
        return userService.findByUsernameOrEmailOrPhone(login);
    }

    @Transactional(readOnly = true)
    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RotatedTokenInfo rotatedTokenInfo = refreshTokenService.rotateRefreshToken(request.getRefreshToken());
        User user = rotatedTokenInfo.getUser();

        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseGet(() -> verificationTrackerService.createTracker(user));

        String newAccessToken = jwtTokenUtil.generateToken(user);

        return AuthResponse.builder()
                .success(true)
                .message("Token refreshed successfully")
                .accessToken(newAccessToken)
                .refreshToken(rotatedTokenInfo.getNewRefreshToken())
                .expiresIn(jwtTokenUtil.getJwtExpirationInMs())
                .user(mapToUserInfo(user, tracker))
                .build();
    }

    @Override
    public String sendWelcomeEmail(String toEmail, String userName) {
        emailService.sendWelcomeEmail(toEmail, userName);
        return "Welcome email sent to " + toEmail;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) throws Exception {
        validateRegistration(request);
        User user = createUser(request);
        User userWithRoles = assignUserRole(user, request.getUserType());
        User savedUser = userService.create(userWithRoles);
        VerificationTracker tracker = verificationTrackerService.createTracker(savedUser);
        return AuthResponse.builder()
                .success(true)
                .message("Registration successful. Please verify your email and phone number.")
                .user(mapToUserInfo(savedUser, tracker))
                ///.isFirstLogin(true)
                ///.requiredSteps(List.of("EMAIL_VERIFICATION", "PHONE_VERIFICATION"))
                .build();
    }

    @Override
    public VerificationResponse verifyEmail(EmailVerificationCodeRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getLinkValidationCode() == null ||
                !user.getLinkValidationCode().equals(request.getVerificationCode()) ||
                user.getExpirationLinkDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired verification code");
        }

        // Mettre à jour le tracker
        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseGet(() -> verificationTrackerService.createTracker(user));

        tracker.setEmailVerified(true);
        tracker.setEmailVerifiedAt(LocalDateTime.now());
        verificationTrackerService.saveTracker(tracker);

        // Activer le compte
        user.setEnabled(true);
        user.setLinkValidationCode(null);
        user.setExpirationLinkDate(null);

        // Déterminer le prochain status
        AccountStatus nextStatus = determineNextStatus(user, tracker);
        user.setStatus(nextStatus);

        user.setStatus(AccountStatus.PENDING_PHONE_VERIFICATION);
        userService.updateStatusAndVerification(user);
        // Envoyer OTP si pas encore fait
        if (!tracker.getPhoneVerified()) {
            otpService.sendOtp(OtpRequest.builder().phone(user.getPhone()).build());
        }
        return VerificationResponse.builder()
                .success(true)
                .message("Email verified successfully")
                .verified(true)
                .phone(user.getPhone())
                .build();
    }

    @Override
    public VerificationResponse resendEmailVerification(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return VerificationResponse.builder()
                    .success(false)
                    .message("User not found")
                    .build();
        }

        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseGet(() -> verificationTrackerService.createTracker(user));

        // Vérifier les tentatives
        if (tracker.getEmailVerificationAttempts() >= 5) {
            throw new RuntimeException("Too many attempts. Try again in 24 hours");
        }

        // Générer nouveau code
        String newCode = generateVerificationCode();
        user.setLinkValidationCode(newCode);
        user.setExpirationLinkDate(LocalDateTime.now().plusHours(24));
        userService.create(user);

        // Tracker les tentatives
        tracker.setEmailVerificationAttempts(tracker.getEmailVerificationAttempts() + 1);
        tracker.setLastEmailAttempt(LocalDateTime.now());
        verificationTrackerService.saveTracker(tracker);

        emailService.sendVerificationEmail(email, user.getFullName(), newCode);

        return VerificationResponse.builder()
                .success(true)
                .message("Verification code resent to email")
                .build();
    }

    @Override
    public VerificationResponse resendPhoneVerification(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return VerificationResponse.builder()
                    .success(false)
                    .message("User not found")
                    .build();
        }

        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseGet(() -> verificationTrackerService.createTracker(user));

        // Vérifier les tentatives
        if (tracker.getPhoneVerificationAttempts() >= 5) {
            throw new RuntimeException("Too many attempts. Try again in 24 hours");
        }

        // Envoyer OTP
        otpService.sendOtp(OtpRequest.builder().phone(user.getPhone()).build());

        // Tracker les tentatives
        tracker.setPhoneVerificationAttempts(tracker.getPhoneVerificationAttempts() + 1);
        tracker.setLastPhoneAttempt(LocalDateTime.now());
        verificationTrackerService.saveTracker(tracker);

        return VerificationResponse.builder()
                .success(true)
                .message("OTP sent to phone")
                .build();
    }

    @Override
    public VerificationStatusResponse getVerificationStatus() {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Tracker not found"));

        return VerificationStatusResponse.builder()
                .emailVerified(tracker.getEmailVerified())
                .phoneVerified(tracker.getPhoneVerified())
                .documentsVerified(tracker.getDocumentsVerified())
                .portfolioVerified(tracker.getPortfolioVerified())
                .serviceZoneSetup(tracker.getServiceZoneSetup())
                .currentStatus(user.getStatus())
                .completionPercentage(tracker.getCompletionPercentage())
                .isComplete(tracker.isVerificationComplete())
                .nextSteps(getRequiredSteps(user, tracker))
                .build();
    }

    @Override
    public UserInfo getUserInfo() {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Tracker not found"));

        return mapToUserInfo(user, tracker);
    }


    @Override
    public LogoutResponse logout(LogoutRequest request) {
        if (request.getRefreshToken() != null) {
            refreshTokenService.deleteByToken(request.getRefreshToken());
        }

        if (request.isLogoutFromAllDevices()) {
            // TODO: Implémenter la déconnexion de tous les appareils
            // Ceci nécessiterait une liste noire des tokens ou une approche similaire
        }

        return LogoutResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build();
    }

    @Override
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            // Ne pas révéler si l'email existe ou non
            return PasswordResetResponse.builder()
                    .success(true)
                    .message("If your email is registered, you will receive a password reset link")
                    .build();
        }

        // Générer le code de réinitialisation
        String resetCode = generateVerificationCode();
        user.setLinkValidationCode(resetCode);
        user.setExpirationLinkDate(LocalDateTime.now().plusHours(1)); // 1 heure d'expiration
        userService.create(user);

        // Envoyer l'email de réinitialisation
        emailService.sendPasswordResetEmail(user.getEmail(),user.getFullName(), resetCode);

        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset code sent to your email")
                .build();
    }


    @Override
    public PasswordResetResponse validateResetCode(ValidateResetCodeRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getLinkValidationCode() == null ||
                !user.getLinkValidationCode().equals(request.getResetCode()) ||
                user.getExpirationLinkDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired reset code");
        }

        user.setLinkValidationCode(null);
        user.setExpirationLinkDate(null);
        userService.updateStatusAndVerification(user);

        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset code validated")
                .build();
    }

    @Override
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        // Réinitialiser le mot de passe
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChanged(true);
        userService.update(user);

        // Invalider tous les refresh tokens existants
        refreshTokenService.deleteByUserId(user.getId());

        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .build();
    }

    // Extract common logic
    private void trackVerificationAttempt(VerificationTracker tracker, String type) {
        if (type.equals("EMAIL")) {
            if (tracker.getEmailVerificationAttempts() >= 5) {
                throw new RuntimeException("Too many attempts. Try again in 24 hours");
            }
            tracker.setEmailVerificationAttempts(tracker.getEmailVerificationAttempts() + 1);
            tracker.setLastEmailAttempt(LocalDateTime.now());
        } else if (type.equals("PHONE")) {
            if (tracker.getPhoneVerificationAttempts() >= 5) {
                throw new RuntimeException("Too many attempts. Try again in 24 hours");
            }
            tracker.setPhoneVerificationAttempts(tracker.getPhoneVerificationAttempts() + 1);
            tracker.setLastPhoneAttempt(LocalDateTime.now());
        }
        verificationTrackerService.update(tracker);
    }

    // Reuse everywhere
    @Override
    public VerificationResponse sendEmailVerification(EmailVerificationRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user == null) throw new RuntimeException("User not found");

        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseGet(() -> verificationTrackerService.createTracker(user));

        trackVerificationAttempt(tracker, "EMAIL");

        if (user.isEnabled()) {
            return VerificationResponse.builder()
                    .success(true)
                    .message("Email already verified")
                    .verified(true)
                    .build();
        }

        String code = generateVerificationCode();
        user.setLinkValidationCode(code);
        user.setExpirationLinkDate(LocalDateTime.now().plusHours(24));
        userService.update(user);

        emailService.sendVerificationEmail(request.getEmail(), user.getFullName(), code);

        return VerificationResponse.builder()
                .success(true)
                .message("Verification email sent")
                .verified(false)
                .build();
    }

    private void validateRegistration(RegisterRequest request) {
        if (userService.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPhone() != null && userService.findByPhone(request.getPhone()) != null) {
            throw new RuntimeException("Phone number already exists");
        }

        if (request.getEmail() == null || !Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", request.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
    }

    private User createUser(RegisterRequest request) {
        User user;
        String userType = request.getUserType().toUpperCase();
        String emailCode = generateVerificationCode();
        if ("CLIENT".equals(userType)) {
            user = new Client();
        } else if ("BARBER".equals(userType)) {
            user = new Barber();
            ((Barber) user).setAverageRating(0.0);
        } else {
            throw new IllegalArgumentException("Invalid user type specified: " + request.getUserType());
        }
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setFullName(request.getFirstName() + " " + request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(false);
        user.setLinkValidationCode(emailCode);
        user.setExpirationLinkDate(LocalDateTime.now().plusHours(24));
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), emailCode);
        return user;
    }

    public User assignUserRole(User user, String userType) {
        Role role = roleService.findByAuthority("ROLE_" + userType.toUpperCase());
        if (role == null) {
            throw new RuntimeException("Role not found: " + userType);
        }

        RoleUser roleUser = new RoleUser();
        roleUser.setUserApp(user);
        roleUser.setRole(role);

        user.setRoleUsers(Collections.singletonList(roleUser));
        return user;
    }

    private UserInfo mapToUserInfo(User user, VerificationTracker tracker) {
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .createdOn(DateUtil.dateToString(user.getCreatedAt()))
                .updatedOn(DateUtil.dateToString(user.getUpdatedAt()))
                .avatar(user.getAvatar())
                .userType(getUserType(user))
                .role(getUserRoles(user).get(0).split("_")[1])
                .accountCompletionPercentage(tracker.getCompletionPercentage())
                .cinVerified(tracker.getCinVerified())
                .documentsVerified(tracker.getDocumentsVerified())
                .portfolioVerified(tracker.getPortfolioVerified())
                .serviceZoneSetup(tracker.getServiceZoneSetup())
                .emailVerified(tracker.getEmailVerified())
                .phoneVerified(tracker.getPhoneVerified())
                .profileCompleted(tracker.isVerificationComplete())
                .build();
    }

    private String getUserType(User user) {
        List<String> roles = getUserRoles(user);
        return roles.isEmpty() ? "CLIENT" : roles.get(0);
    }

    private List<String> getUserRoles(User user) {
        if (user.getRoleUsers() == null) {
            return Collections.emptyList();
        }
        return user.getRoleUsers().stream()
                .map(roleUser -> roleUser.getRole().getAuthority())
                .collect(Collectors.toList());
    }

    private List<String> getRequiredOnboardingSteps(User user, VerificationTracker tracker) {
        return getRequiredSteps(user, tracker);
    }

    public static AccountStatus determineNextStatus(User user, VerificationTracker tracker) {
        // Email pas vérifié
        if (!tracker.getEmailVerified()) {
            return AccountStatus.PENDING_EMAIL_VERIFICATION;
        }

        // Email vérifié, téléphone pas encore
        if (!tracker.getPhoneVerified()) {
            return AccountStatus.PENDING_PHONE_VERIFICATION;
        }

        // Pour les barbiers
        if (isBarber(user)) {
            if(!tracker.getCinVerified()){
                return AccountStatus.PENDING_CIN_VERIFICATION;
            }
            if (!tracker.getDocumentsVerified()) {
                return AccountStatus.PENDING_DOCUMENT_VERIFICATION;
            }
            if (!tracker.getPortfolioVerified()) {
                return AccountStatus.PENDING_PORTFOLIO_UPLOAD;
            }
            if (!tracker.getServiceZoneSetup()) {
                return AccountStatus.PENDING_SERVICE_ZONE_SETUP;
            }
        }

        // Tout est complet
        return AccountStatus.ACTIVE;
    }

    public static List<String> getRequiredSteps(User user, VerificationTracker tracker) {
        List<String> steps = new ArrayList<>();

        if (!tracker.getEmailVerified()) {
            steps.add("EMAIL_VERIFICATION");
        }
        if (!tracker.getPhoneVerified()) {
            steps.add("PHONE_VERIFICATION");
        }
        if (isBarber(user)) {
            if (!tracker.getCinVerified()) {
                steps.add("CIN_VERIFICATION");
            }
            if (!tracker.getDocumentsVerified()) {
                steps.add("DOCUMENT_VERIFICATION");
            }
            if (!tracker.getPortfolioVerified()) {
                steps.add("PORTFOLIO_UPLOAD");
            }
            if (!tracker.getServiceZoneSetup()) {
                steps.add("SERVICE_ZONE_SETUP");
            }
        }

        return steps;
    }

    public static String getNextStep(User user, VerificationTracker tracker) {
        return determineNextStatus(user, tracker).getDescription();
    }

    public static boolean isBarber(User user) {
        return user.getRoleUsers().stream()
                .anyMatch(ru -> ru.getRole().getAuthority().equals("ROLE_BARBER"));
    }

    private boolean isFirstLogin(User user) {
        return user.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5));
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 900000) + 100000);
    }

}