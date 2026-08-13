package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.AccessTokenResponse;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.jwt.JwtClaims;
import com.sdp1617.backend.auth.jwt.JwtProvider;
import com.sdp1617.backend.auth.jwt.TokenType;
import com.sdp1617.backend.auth.repository.RefreshTokenRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void issueTokens는_accessToken과_refreshToken을_발급하고_refreshToken을_저장한다() {
        when(jwtProvider.createAccessToken(1L)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn("refresh-token");

        TokenResponse response = tokenService.issueTokens(1L);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenRepository).save(org.mockito.ArgumentMatchers.eq(1L), any());
    }

    @Test
    void 저장된_세션이_존재하면_새_accessToken을_발급한다() {
        String refreshToken = "refresh-token";
        when(jwtProvider.parse(refreshToken, TokenType.REFRESH)).thenReturn(new JwtClaims(1L, "session-1"));
        when(refreshTokenRepository.exists(1L, "session-1")).thenReturn(true);
        when(jwtProvider.createAccessToken(1L)).thenReturn("new-access-token");

        AccessTokenResponse response = tokenService.reissue(refreshToken);

        assertEquals("new-access-token", response.accessToken());
    }

    @Test
    void 저장된_세션이_없으면_AUTH_005_예외를_던진다() {
        String refreshToken = "refresh-token";
        when(jwtProvider.parse(refreshToken, TokenType.REFRESH)).thenReturn(new JwtClaims(1L, "session-1"));
        when(refreshTokenRepository.exists(1L, "session-1")).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> tokenService.reissue(refreshToken));

        assertEquals(ErrorCode.AUTH_005, exception.getErrorCode());
    }

    @Test
    void revokeSession은_해당_세션만_삭제한다() {
        String refreshToken = "refresh-token";
        when(jwtProvider.parse(refreshToken, TokenType.REFRESH)).thenReturn(new JwtClaims(1L, "session-1"));

        tokenService.revokeSession(1L, refreshToken);

        verify(refreshTokenRepository).deleteOne(1L, "session-1");
    }

    @Test
    void revokeSession시_토큰의_memberId가_다르면_AUTH_003_예외를_던진다() {
        String refreshToken = "refresh-token";
        when(jwtProvider.parse(refreshToken, TokenType.REFRESH)).thenReturn(new JwtClaims(2L, "session-1"));

        CustomException exception = assertThrows(CustomException.class, () -> tokenService.revokeSession(1L, refreshToken));

        assertEquals(ErrorCode.AUTH_003, exception.getErrorCode());
    }
}
