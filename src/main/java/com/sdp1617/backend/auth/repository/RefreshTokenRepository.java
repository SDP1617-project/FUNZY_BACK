package com.sdp1617.backend.auth.repository;

import com.sdp1617.backend.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh-token:";
    private static final String VALID_MARKER = "valid";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long memberId, String tokenId) {
        redisTemplate.opsForValue().set(
                key(memberId, tokenId),
                VALID_MARKER,
                Duration.ofMillis(jwtProperties.refreshExpiration())
        );
    }

    public boolean exists(Long memberId, String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(memberId, tokenId)));
    }

    private String key(Long memberId, String tokenId) {
        return KEY_PREFIX + memberId + ":" + tokenId;
    }
}
