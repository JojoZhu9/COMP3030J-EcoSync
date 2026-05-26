package com.example.bibilabo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(
                "RWNvU3luY1N1cGVyU2VjcmV0S2V5Rm9ySldUQXV0aGVudGljYXRpb24yMDI2",
                86400000L
        );
    }

    @Test
    void generateAndParseToken_preservesClaims() {
        String token = jwtUtils.generateToken(12, "user_alice", "CONSUMER", null);
        assertNotNull(token);

        Claims claims = jwtUtils.parseToken(token);
        assertEquals(12, claims.get("userId", Integer.class));
        assertEquals("user_alice", claims.get("username", String.class));
        assertEquals("CONSUMER", claims.get("role", String.class));
    }

    @Test
    void parseInvalidToken_throwsException() {
        assertThrows(Exception.class, () -> jwtUtils.parseToken("not.a.valid.token"));
    }

    @Test
    void parseExpiredToken_throwsException() {
        JwtUtils shortLivedJwtUtils = new JwtUtils(
                "RWNvU3luY1N1cGVyU2VjcmV0S2V5Rm9ySldUQXV0aGVudGljYXRpb24yMDI2",
                -1000L
        );
        String token = shortLivedJwtUtils.generateToken(1, "test", "CONSUMER", null);

        assertThrows(ExpiredJwtException.class, () -> jwtUtils.parseToken(token));
    }

    @Test
    void parseTokenFromDifferentKey_throwsException() {
        JwtUtils otherJwtUtils = new JwtUtils(
                "RGlmZmVyZW50U2VjcmV0S2V5Rm9yVGVzdGluZ1B1cnBvc2VzT25seQ==",
                86400000L
        );
        String token = otherJwtUtils.generateToken(1, "test", "CONSUMER", null);

        assertThrows(Exception.class, () -> jwtUtils.parseToken(token));
    }
}
