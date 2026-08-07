package com.sdp1617.backend.auth.jwt;

import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String TYPE_CLAIM = "type";

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = jwtProperties.accessExpiration();
        this.refreshExpiration = jwtProperties.refreshExpiration();
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessExpiration, TokenType.ACCESS, null);
    }

    public String createRefreshToken(Long memberId, String tokenId) {
        return createToken(memberId, refreshExpiration, TokenType.REFRESH, tokenId);
    }

    public JwtClaims parse(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get(TYPE_CLAIM, String.class);
            if (!expectedType.name().equals(type)) {
                throw new CustomException(ErrorCode.AUTH_003);
            }

            return new JwtClaims(Long.valueOf(claims.getSubject()), claims.getId());
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.AUTH_004);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTH_003);
        }
    }

    private String createToken(Long memberId, long expiration, TokenType type, String tokenId) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TYPE_CLAIM, type.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration));

        if (tokenId != null) {
            builder.id(tokenId);
        }

        return builder.signWith(key).compact();
    }
}
