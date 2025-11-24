package com.halaq.backend.core.security.service.impl;

import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.controller.dto.request.OtpRequest;
import com.halaq.backend.core.security.controller.dto.request.OtpVerificationRequest;
import com.halaq.backend.core.security.controller.dto.response.AuthResponse;
import com.halaq.backend.core.security.controller.dto.response.OtpResponse;
import com.halaq.backend.core.security.entity.OtpCode;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.enums.AccountStatus;
import com.halaq.backend.core.security.jwt.JwtTokenUtil;
import com.halaq.backend.core.security.repository.facade.core.OtpCodeRepository;
import com.halaq.backend.core.security.service.facade.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.halaq.backend.core.security.service.impl.AuthServiceImpl.determineNextStatus;

@Service
@Transactional
public class  OtpServiceImpl implements OtpService {

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxOtpAttempts;

    @Value("${app.otp.resend-cooldown-minutes:1}")
    private int resendCooldownMinutes;

    private final VerificationTrackerService verificationTrackerService;

    private final Random random = new Random();

    public OtpServiceImpl(VerificationTrackerService verificationTrackerService) {
        this.verificationTrackerService = verificationTrackerService;
    }

    @Override
    public OtpResponse sendOtp(OtpRequest request) {
        String normalizedPhone = normalizePhoneNumber(request.getPhone());

        // Vérifier le cooldown pour l'envoi
        Optional<OtpCode> existingOtp = otpCodeRepository.findByPhoneAndUsedFalse(normalizedPhone);
        if (existingOtp.isPresent() &&
                existingOtp.get().getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(resendCooldownMinutes))) {
            throw new RuntimeException("Please wait before requesting another OTP");
        }

        // Invalider tous les OTP existants pour ce numéro
        otpCodeRepository.invalidateByPhone(normalizedPhone);

        // Générer nouveau OTP
        String otpCode = generateOtpCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // Sauvegarder l'OTP
        OtpCode otp = new OtpCode();
        otp.setPhone(normalizedPhone);
        otp.setCode(otpCode);
        otp.setAction(request.getAction());
        otp.setUserType(request.getUserType());
        otp.setExpiresAt(expiresAt);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setAttempts(0);
        otp.setUsed(false);

        otpCodeRepository.save(otp);

        // Envoyer le SMS
        String message = String.format("Your Halaq.ma verification code is: %s. Valid for %d minutes.",
                otpCode, otpExpirationMinutes);
        smsService.sendSms(normalizedPhone, message);

        return OtpResponse.builder()
                .success(true)
                .message("OTP sent successfully")
                .phone(maskPhoneNumber(normalizedPhone))
                .expiresAt(expiresAt)
                .expiresInMinutes(otpExpirationMinutes)
                .build();
    }

    @Override
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        String normalizedPhone = normalizePhoneNumber(request.getPhone());
        // Chercher l'OTP valide
        Optional<OtpCode> otpOptional = otpCodeRepository.findByPhoneAndUsedFalse(normalizedPhone);
        if (otpOptional.isEmpty()) {
            throw new RuntimeException("No valid OTP found for this phone number");
        }
        OtpCode otp = otpOptional.get();
        // Vérifier l'expiration
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCodeRepository.delete(otp);
            throw new RuntimeException("OTP has expired");
        }
        // Vérifier le nombre de tentatives
        if (otp.getAttempts() >= maxOtpAttempts) {
            otpCodeRepository.delete(otp);
            throw new RuntimeException("Maximum OTP attempts exceeded");
        }
        // Incrémenter les tentatives
        otp.setAttempts(otp.getAttempts() + 1);
        otpCodeRepository.save(otp);

        // Vérifier le code
        if (!otp.getCode().equals(request.getOtpCode())) {
            if (otp.getAttempts() >= maxOtpAttempts) {
                otpCodeRepository.delete(otp);
            }
            throw new RuntimeException("Invalid OTP code");
        }

        // Marquer l'OTP comme utilisé
        otp.setUsed(true);
        otp.setUsedAt(LocalDateTime.now());
        otpCodeRepository.save(otp);

        // Chercher ou créer l'utilisateur
        User user = userService.findByPhone(normalizedPhone);
        VerificationTracker tracker = verificationTrackerService.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Tracker not found"));
        // Mettre à jour le tracker
        tracker.setPhoneVerified(true);
        tracker.setPhoneVerifiedAt(LocalDateTime.now());
        verificationTrackerService.saveTracker(tracker);

        return AuthResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .build();
    }

    @Override
    public User createOtpUser(OtpVerificationRequest request) {
        String normalizedPhone = normalizePhoneNumber(request.getPhone());

        User user = new User();
        user.setPhone(normalizedPhone);
        user.setUsername(normalizedPhone);

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        } else {
            // Email temporaire basé sur le téléphone
            user.setEmail(normalizedPhone.replaceAll("\\+", "") + "@halaq.temp");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (user.getFirstName() != null && user.getLastName() != null) {
            user.setFullName(user.getFirstName() + " " + user.getLastName());
        }

        user.setEnabled(true); // Le téléphone est déjà vérifié via OTP
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Générer un mot de passe aléatoire (non utilisé mais requis)
        user.setPassword(java.util.UUID.randomUUID().toString());

        userService.create(user);
        assignUserRole(user, request.getUserType());

        return user;
    }

    @Override
    public void assignUserRole(User user, String userType) {
        // Cette méthode devra être implémentée selon votre système de rôles
        switch (userType.toUpperCase()) {
            case "CLIENT":
                // Assigner le rôle CLIENT
                break;
            case "BARBER":
                // Assigner le rôle BARBER
                break;
            default:
                // Rôle par défaut CLIENT
                break;
        }
    }

    @Override
    public UserInfo mapToUserInfo(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .userType(getUserType(user))
                .emailVerified(user.isEnabled() && !user.getEmail().contains("@halaq.temp"))
                .phoneVerified(true) // Toujours vrai après vérification OTP
                .profileCompleted(isProfileCompleted(user))
                .build();
    }

    @Override
    public String getUserType(User user) {
        // Implémenter selon votre logique de rôles
        return "CLIENT"; // À adapter
    }

    @Override
    public boolean isProfileCompleted(User user) {
        return user.getFirstName() != null &&
                user.getLastName() != null &&
                user.getPhone() != null &&
                user.isEnabled() &&
                !user.getEmail().contains("@halaq.temp");
    }

    @Override
    public List<String> getUserRoles(User user) {
        return Arrays.asList("ROLE_CLIENT");
    }

    @Override
    public List<String> getRequiredOnboardingSteps(User user) {
        List<String> steps = new java.util.ArrayList<>();

        if (user.getEmail().contains("@halaq.temp")) {
            steps.add("EMAIL_SETUP");
        }

        if (user.getFirstName() == null || user.getLastName() == null) {
            steps.add("PROFILE_COMPLETION");
        }

        // Pour les coiffeurs, étapes supplémentaires
        if ("BARBER".equals(getUserType(user))) {
            steps.add("DOCUMENT_VERIFICATION");
            steps.add("PORTFOLIO_UPLOAD");
            steps.add("SERVICE_ZONE_SETUP");
        }

        return steps;
    }

    @Override
    public String generateOtpCode() {
        return String.format("%06d", random.nextInt(900000) + 100000);
    }

    @Override
    public String normalizePhoneNumber(String phone) {
        // Normaliser le format du numéro marocain
        String normalized = phone.replaceAll("\\s+", "").replaceAll("-", "");

        if (normalized.startsWith("00212")) {
            normalized = "+" + normalized.substring(2);
        } else if (normalized.startsWith("0")) {
            normalized = "+212" + normalized.substring(1);
        } else if (!normalized.startsWith("+212")) {
            normalized = "+212" + normalized;
        }

        return normalized;
    }

    @Override
    public String maskPhoneNumber(String phone) {
        if (phone.length() > 6) {
            return phone.substring(0, phone.length() - 6) + "****" + phone.substring(phone.length() - 2);
        }
        return phone;
    }
}
