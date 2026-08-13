package com.sdp1617.backend.auth.repository;

import com.sdp1617.backend.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh-token:";
    private static final String SESSIONS_KEY_PREFIX = "refresh-token-sessions:";
    private static final String VALID_MARKER = "valid";

    private static final RedisScript<Long> SAVE_SCRIPT = new DefaultRedisScript<>(
            "redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2]) " +
                    "redis.call('SADD', KEYS[2], ARGV[3]) " +
                    "redis.call('PEXPIRE', KEYS[2], ARGV[2]) " +
                    "return 1",
            Long.class
    );

    private static final RedisScript<Long> DELETE_ONE_SCRIPT = new DefaultRedisScript<>(
            "redis.call('DEL', KEYS[1]) " +
                    "redis.call('SREM', KEYS[2], ARGV[1]) " +
                    "return 1",
            Long.class
    );

    private static final RedisScript<Long> DELETE_ALL_SCRIPT = new DefaultRedisScript<>(
            "local tokenIds = redis.call('SMEMBERS', KEYS[1]) " +
                    "for _, tokenId in ipairs(tokenIds) do " +
                    "  redis.call('DEL', ARGV[1] .. tokenId) " +
                    "end " +
                    "redis.call('DEL', KEYS[1]) " +
                    "return #tokenIds",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long memberId, String tokenId) {
        String ttlMillis = String.valueOf(jwtProperties.refreshExpiration());
        redisTemplate.execute(
                SAVE_SCRIPT,
                List.of(key(memberId, tokenId), sessionsKey(memberId)),
                VALID_MARKER, ttlMillis, tokenId
        );
    }

    public boolean exists(Long memberId, String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(memberId, tokenId)));
    }

    public void deleteOne(Long memberId, String tokenId) {
        redisTemplate.execute(
                DELETE_ONE_SCRIPT,
                List.of(key(memberId, tokenId), sessionsKey(memberId)),
                tokenId
        );
    }

    public void deleteAllByMemberId(Long memberId) {
        redisTemplate.execute(
                DELETE_ALL_SCRIPT,
                List.of(sessionsKey(memberId)),
                KEY_PREFIX + memberId + ":"
        );
    }

    private String key(Long memberId, String tokenId) {
        return KEY_PREFIX + memberId + ":" + tokenId;
    }

    private String sessionsKey(Long memberId) {
        return SESSIONS_KEY_PREFIX + memberId;
    }
}
