package com.elearning.enrollment;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public final class TestJwtFactory {

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-only-not-for-production-use-1234567890";

    private TestJwtFactory() {}

    public static String token(Long userId, String email, String... roles) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 900_000);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("roles", List.of(roles))
                .issuer("elearning-auth-service-test")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
