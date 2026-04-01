package com.healthcare.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TokenBlacklistService} — happy path only.
 */
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void blacklist_writesKeyWithCorrectTtl() {
        Claims mockClaims = mock(Claims.class);
        when(jwtService.extractJti(mockClaims)).thenReturn("jti-abc-123");
        when(jwtService.getRemainingTtlSeconds(mockClaims)).thenReturn(300L);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        tokenBlacklistService.blacklist(mockClaims);

        verify(valueOps).set(eq("blacklist:jti-abc-123"), eq("1"), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void isBlacklisted_returnsTrueWhenKeyExists() {
        when(redisTemplate.hasKey("blacklist:jti-abc-123")).thenReturn(true);

        assertThat(tokenBlacklistService.isBlacklisted("jti-abc-123")).isTrue();
    }

    @Test
    void isBlacklisted_returnsFalseWhenKeyAbsent() {
        when(redisTemplate.hasKey("blacklist:jti-abc-123")).thenReturn(false);

        assertThat(tokenBlacklistService.isBlacklisted("jti-abc-123")).isFalse();
    }
}
