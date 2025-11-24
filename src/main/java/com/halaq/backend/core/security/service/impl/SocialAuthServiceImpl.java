package com.halaq.backend.core.security.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.controller.dto.request.AppleAuthRequest;
import com.halaq.backend.core.security.controller.dto.request.FacebookAuthRequest;
import com.halaq.backend.core.security.controller.dto.request.GoogleAuthRequest;
import com.halaq.backend.core.security.controller.dto.response.AuthResponse;
import com.halaq.backend.core.security.entity.Role;
import com.halaq.backend.core.security.entity.RoleUser;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.jwt.JwtTokenUtil;
import com.halaq.backend.core.security.service.facade.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SocialAuthServiceImpl implements SocialAuthService {

    private final UserService userService;

    private final JwtTokenUtil jwtTokenUtil;

    private final RefreshTokenService refreshTokenService;

    private RoleService roleService;

    private final AuthService authService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id:}")
    private String facebookAppId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret:}")
    private String facebookAppSecret;

    @Value("${spring.security.oauth2.client.registration.apple.client-id:}")
    private String appleClientId;

    public SocialAuthServiceImpl(UserService userService, JwtTokenUtil jwtTokenUtil, RefreshTokenService refreshTokenService, AuthService authService) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.refreshTokenService = refreshTokenService;
        this.authService = authService;
    }

    @Override
    public AuthResponse googleLogin(GoogleAuthRequest request) throws Exception {
        try {
            // Vérifier le token Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String picture = (String) payload.get("picture");
            String googleId = payload.getSubject();

            // Chercher ou créer l'utilisateur
            User user = userService.findByEmail(email);
            boolean isNewUser = false;

            if (user == null) {
                // Créer un nouvel utilisateur
                user = createSocialUser(email, firstName, lastName, picture, "GOOGLE", googleId, request.getUserType());
                isNewUser = true;
            } else {
                // Mettre à jour les informations si nécessaire
                updateUserSocialInfo(user, firstName, lastName, picture, "GOOGLE", googleId);
            }

            // Générer les tokens
            String accessToken = jwtTokenUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

            return AuthResponse.builder()
                    .success(true)
                    .message("Google login successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtTokenUtil.getJwtExpirationInMs())
                    .user(mapToUserInfo(user))
                    .isFirstLogin(isNewUser)
                    .requiredSteps(getRequiredOnboardingSteps(user))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse facebookLogin(FacebookAuthRequest request) throws Exception {
        try {
            // Vérifier le token Facebook
            String url = String.format("https://graph.facebook.com/me?access_token=%s&fields=id,email,first_name,last_name,picture",
                    request.getAccessToken());

            Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

            if (userInfo == null) {
                throw new RuntimeException("Invalid Facebook token");
            }

            String email = (String) userInfo.get("email");
            String firstName = (String) userInfo.get("first_name");
            String lastName = (String) userInfo.get("last_name");
            String facebookId = (String) userInfo.get("id");

            // Extraire l'URL de la photo
            String picture = null;
            if (userInfo.get("picture") instanceof Map) {
                Map<String, Object> pictureData = (Map<String, Object>) ((Map<String, Object>) userInfo.get("picture")).get("data");
                picture = (String) pictureData.get("url");
            }

            if (email == null) {
                throw new RuntimeException("Email permission required for Facebook login");
            }

            // Chercher ou créer l'utilisateur
            User user = userService.findByEmail(email);
            boolean isNewUser = false;

            if (user == null) {
                user = createSocialUser(email, firstName, lastName, picture, "FACEBOOK", facebookId, request.getUserType());
                isNewUser = true;
            } else {
                updateUserSocialInfo(user, firstName, lastName, picture, "FACEBOOK", facebookId);
            }

            // Générer les tokens
            String accessToken = jwtTokenUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

            return AuthResponse.builder()
                    .success(true)
                    .message("Facebook login successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtTokenUtil.getJwtExpirationInMs())
                    .user(mapToUserInfo(user))
                    .isFirstLogin(isNewUser)
                    .requiredSteps(getRequiredOnboardingSteps(user))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Facebook authentication failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse appleLogin(AppleAuthRequest request) throws Exception {
        try {
            // Pour Apple Sign In, il faut décoder le JWT identity token
            // Ceci est une implémentation simplifiée - en production, vous devez vérifier la signature
            String[] chunks = request.getIdentityToken().split("\\.");

            if (chunks.length != 3) {
                throw new RuntimeException("Invalid Apple identity token format");
            }

            // Décoder le payload (simplifié - en production, utilisez une bibliothèque JWT appropriée)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);

            String email = (String) claims.get("email");
            String appleId = (String) claims.get("sub");

            // Apple ne fournit pas toujours le prénom/nom, ils peuvent être dans l'authorization code
            String firstName = "User"; // Valeur par défaut
            String lastName = "";

            if (email == null) {
                throw new RuntimeException("Email not provided by Apple");
            }

            // Chercher ou créer l'utilisateur
            User user = userService.findByEmail(email);
            boolean isNewUser = false;

            if (user == null) {
                user = createSocialUser(email, firstName, lastName, null, "APPLE", appleId, request.getUserType());
                isNewUser = true;
            } else {
                updateUserSocialInfo(user, firstName, lastName, null, "APPLE", appleId);
            }

            // Générer les tokens
            String accessToken = jwtTokenUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

            return AuthResponse.builder()
                    .success(true)
                    .message("Apple login successful")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtTokenUtil.getJwtExpirationInMs())
                    .user(mapToUserInfo(user))
                    .isFirstLogin(isNewUser)
                    .requiredSteps(getRequiredOnboardingSteps(user))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Apple authentication failed: " + e.getMessage());
        }
    }

    private User createSocialUser(String email, String firstName, String lastName,
                                  String picture, String provider, String providerId, String userType) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setFullName((firstName + " " + (lastName != null ? lastName : "")).trim());
        user.setAvatar(picture);
        user.setEnabled(true); // Les comptes sociaux sont automatiquement vérifiés
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Générer un mot de passe aléatoire (non utilisé mais requis)
        user.setPassword(java.util.UUID.randomUUID().toString());

        // Ajouter des métadonnées sur le provider social
        user.setAbout(String.format("Connected via %s (ID: %s)", provider, providerId));
        User userWithRole = authService.assignUserRole(user,userType);
        return userService.create(userWithRole);

    }

    private void updateUserSocialInfo(User user, String firstName, String lastName,
                                      String picture, String provider, String providerId) {
        boolean updated = false;

        if (firstName != null && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
            updated = true;
        }

        if (lastName != null && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
            updated = true;
        }

        if (updated) {
            user.setFullName((user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "")).trim());
        }

        if (picture != null && !picture.equals(user.getAvatar())) {
            user.setAvatar(picture);
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            userService.create(user);
        }
    }


    private UserInfo mapToUserInfo(User user) {
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
                .emailVerified(user.isEnabled())
                .phoneVerified(isPhoneVerified(user))
                .profileCompleted(isProfileCompleted(user))
                .build();
    }

    private String getUserType(User user) {
        return user.getRoleUsers().stream().map(RoleUser::getRole).map(Role::getAuthority).findFirst().orElse(null);
    }

    private boolean isPhoneVerified(User user) {
        return user.getPhone() != null && !user.getPhone().isEmpty();
    }

    private boolean isEmailVerified(User user) {
        return user.isEnabled();
    }

    private boolean isProfileCompleted(User user) {
        return user.getFirstName() != null &&
                user.getLastName() != null &&
                user.isEnabled();
    }

    private List<String> getUserRoles(User user) {
        return user.getRoleUsers().stream()
                .map(roleUser -> roleUser.getRole().getAuthority())
                .toList();
    }

    private List<String> getRequiredOnboardingSteps(User user) {
        List<String> steps = new java.util.ArrayList<>();

        if (!isPhoneVerified(user)) {
            steps.add("PHONE_VERIFICATION");
        }

        if (!isProfileCompleted(user)) {
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
}
