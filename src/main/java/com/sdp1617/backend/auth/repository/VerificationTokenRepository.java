package com.sdp1617.backend.auth.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VerificationTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public String issue(String purpose, Long memberId, Duration ttl) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key(purpose, token), String.valueOf(memberId), ttl);
        return token;
    }

    public Optional<Long> consume(String purpose, String token) {
        String value = redisTemplate.opsForValue().getAndDelete(key(purpose, token));
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    private String key(String purpose, String token) {
        return purpose + ":" + token;
    }
}
