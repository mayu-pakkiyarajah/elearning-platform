package com.elearning.auth.service;

import com.elearning.auth.entity.RefreshToken;
import com.elearning.auth.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyAndGet(String token);

    void revokeAllForUser(Long userId);
}
