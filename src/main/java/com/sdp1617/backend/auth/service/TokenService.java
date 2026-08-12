package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.AccessTokenResponse;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.jwt.JwtClaims;
import com.sdp1617.backend.auth.jwt.JwtProvider;
import com.sdp1617.backend.auth.jwt.TokenType;
import com.sdp1617.backend.auth.repository.RefreshTokenRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse issueTokens(Long memberId) {
        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId, tokenId);
        refreshTokenRepository.save(memberId, tokenId);
        return new TokenResponse(accessToken, refreshToken);
    }

    public AccessTokenResponse reissue(String refreshToken) {
        JwtClaims claims = jwtProvider.parse(refreshToken, TokenType.REFRESH);

        if (!refreshTokenRepository.exists(claims.memberId(), claims.tokenId())) {
            throw new CustomException(ErrorCode.AUTH_005);
        }

        return new AccessTokenResponse(jwtProvider.createAccessToken(claims.memberId()));
    }

    public void revokeAllSessions(Long memberId) {
        refreshTokenRepository.deleteAllByMemberId(memberId);
    }
}
