package com.halaq.backend.core.security.controller.dto.response;

import com.halaq.backend.core.dto.AuditBaseDto;
import com.halaq.backend.core.security.entity.User;


public class RotatedTokenInfo  extends AuditBaseDto {
    private final String newRefreshToken;
    private final User user;

    public RotatedTokenInfo(String newRefreshToken, User user) {
        this.newRefreshToken = newRefreshToken;
        this.user = user;
    }

    public String getNewRefreshToken() {
        return newRefreshToken;
    }

    public User getUser() {
        return user;
    }
}
