package com.elearning.auth.service.impl;

import com.elearning.auth.config.PasswordResetProperties;
import com.elearning.auth.dto.request.*;
import com.elearning.auth.dto.response.AuthResponse;
import com.elearning.auth.dto.response.UserResponse;
import com.elearning.auth.entity.PasswordResetToken;
import com.elearning.auth.entity.RefreshToken;
import com.elearning.auth.entity.Role;
import com.elearning.auth.entity.User;
import com.elearning.auth.exception.*;
import com.elearning.auth.mapper.UserMapper;
import com.elearning.auth.repository.PasswordResetTokenRepository;
import com.elearning.auth.repository.RoleRepository;
import com.elearning.auth.repository.UserRepository;
import com.elearning.auth.security.JwtService;
import com.elearning.auth.service.AuthService;
import com.elearning.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final PasswordResetProperties passwordResetProperties;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String roleName = "ROLE_" + request.getRequestedRole().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not configured: " + roleName));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountLocked(false)
                // instructors need admin approval before they can publish courses;
                // students don't need approval
                .instructorApproved(!roleName.equals("ROLE_INSTRUCTOR"))
                .roles(Set.of(role))
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getEmail(), roleName);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception ex) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenService.verifyAndGet(request.getRefreshToken());
        User user = existing.getUser();

        // rotate: revoke the used token, issue a fresh pair
        existing.setRevoked(true);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            RefreshToken token = refreshTokenService.verifyAndGet(refreshToken);
            token.setRevoked(true);
        } catch (InvalidRefreshTokenException ex) {
            // already invalid/expired/revoked — logout is idempotent, nothing to do
            log.debug("Logout called with already-invalid refresh token");
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(generateOpaqueToken())
                    .expiresAt(LocalDateTime.now().plusMinutes(passwordResetProperties.getTokenExpirationMinutes()))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // TODO: publish a "PasswordResetRequested" event for notification-service to email the token/link.
            log.info("Password reset token generated for user {} (would be emailed in production)", user.getEmail());
        });
        // Intentionally do not reveal whether the email exists — same response either way.
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidResetTokenException("Reset token is invalid"));

        if (resetToken.isUsed()) {
            throw new InvalidResetTokenException("Reset token has already been used");
        }
        if (resetToken.isExpired()) {
            throw new InvalidResetTokenException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        resetToken.setUsed(true);

        // force re-login everywhere after a password reset
        refreshTokenService.revokeAllForUser(user.getId());

        log.info("Password reset completed for user {}", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresInMs(jwtService.getAccessTokenExpirationMs())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
