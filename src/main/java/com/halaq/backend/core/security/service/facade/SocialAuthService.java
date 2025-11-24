package com.halaq.backend.core.security.service.facade;

import com.halaq.backend.core.security.controller.dto.request.AppleAuthRequest;
import com.halaq.backend.core.security.controller.dto.request.FacebookAuthRequest;
import com.halaq.backend.core.security.controller.dto.request.GoogleAuthRequest;
import com.halaq.backend.core.security.controller.dto.response.AuthResponse;

public interface SocialAuthService {
    AuthResponse googleLogin(GoogleAuthRequest request) throws Exception;

    AuthResponse facebookLogin(FacebookAuthRequest request) throws Exception;

    AuthResponse appleLogin(AppleAuthRequest request) throws Exception;
}
