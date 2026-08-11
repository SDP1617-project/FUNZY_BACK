package com.sdp1617.backend.auth.repository;

import com.sdp1617.backend.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh-token:";
    private static final String SESSIONS_KEY_PREFIX = "refresh-token-sessions:";
    private static final String VALID_MARKER = "valid";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long memberId, String tokenId) {
        Duration ttl = Duration.ofMillis(jwtProperties.refreshExpiration());
        redisTemplate.opsForValue().set(key(memberId, tokenId), VALID_MARKER, ttl);
        redisTemplate.opsForSet().add(sessionsKey(memberId), tokenId);
    }

    public boolean exists(Long memberId, String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(memberId, tokenId)));
    }

    public void deleteAllByMemberId(Long memberId) {
        String sessionsKey = sessionsKey(memberId);
        Set<String> tokenIds = redisTemplate.opsForSet().members(sessionsKey);
        if (tokenIds != null) {
            tokenIds.forEach(tokenId -> redisTemplate.delete(key(memberId, tokenId)));
        }
        redisTemplate.delete(sessionsKey);
    }

    private String key(Long memberId, String tokenId) {
        return KEY_PREFIX + memberId + ":" + tokenId;
    }

    private String sessionsKey(Long memberId) {
        return SESSIONS_KEY_PREFIX + memberId;
    }
}
