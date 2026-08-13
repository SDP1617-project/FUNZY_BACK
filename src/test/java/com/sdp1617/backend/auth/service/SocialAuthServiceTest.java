package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.SocialAuthResponse;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.repository.SocialSignupSessionRepository;
import com.sdp1617.backend.auth.social.SocialSignupSession;
import com.sdp1617.backend.auth.social.SocialUserInfo;
import com.sdp1617.backend.auth.social.SocialUserInfoProvider;
import com.sdp1617.backend.auth.social.SocialUserInfoProviderRegistry;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

    @Mock
    private SocialUserInfoProviderRegistry providerRegistry;

    @Mock
    private SocialUserInfoProvider kakaoProvider;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialSignupSessionRepository socialSignupSessionRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private SocialAuthService socialAuthService;

    @Test
    void 기존_소셜계정이면_바로_토큰을_발급한다() {
        when(providerRegistry.get(AuthProvider.KAKAO)).thenReturn(kakaoProvider);
        when(kakaoProvider.fetchUserInfo("token")).thenReturn(new SocialUserInfo("12345", "test@kakao.com"));

        Member member = new Member("test@kakao.com", "닉네임", true, AuthProvider.KAKAO, "12345");
        setId(member, 1L);
        when(memberRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "12345"))
                .thenReturn(Optional.of(member));
        when(tokenService.issueTokens(1L)).thenReturn(new TokenResponse("access", "refresh"));

        SocialAuthResponse response = socialAuthService.login(AuthProvider.KAKAO, "token");

        assertFalse(response.isNewUser());
        assertEquals("access", response.tokens().accessToken());
    }

    @Test
    void 신규_소셜유저면_signupToken을_발급한다() {
        when(providerRegistry.get(AuthProvider.KAKAO)).thenReturn(kakaoProvider);
        when(kakaoProvider.fetchUserInfo("token")).thenReturn(new SocialUserInfo("12345", "new@kakao.com"));
        when(memberRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(memberRepository.existsByEmail("new@kakao.com")).thenReturn(false);
        when(socialSignupSessionRepository.issue(any(), eq(Duration.ofMinutes(15)))).thenReturn("signup-token");

        SocialAuthResponse response = socialAuthService.login(AuthProvider.KAKAO, "token");

        assertTrue(response.isNewUser());
        assertEquals("signup-token", response.signupToken());
        assertEquals("new@kakao.com", response.email());
    }

    @Test
    void 이미_다른_방식으로_가입된_이메일이면_AUTH_012_예외를_던진다() {
        when(providerRegistry.get(AuthProvider.KAKAO)).thenReturn(kakaoProvider);
        when(kakaoProvider.fetchUserInfo("token")).thenReturn(new SocialUserInfo("12345", "existing@sdp1617.com"));
        when(memberRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(memberRepository.existsByEmail("existing@sdp1617.com")).thenReturn(true);

        CustomException exception = assertThrows(
                CustomException.class, () -> socialAuthService.login(AuthProvider.KAKAO, "token"));

        assertEquals(ErrorCode.AUTH_012, exception.getErrorCode());
    }

    @Test
    void 소셜_회원가입을_완료하면_토큰을_발급한다() {
        SocialSignupSession session = new SocialSignupSession(AuthProvider.KAKAO, "12345", "test@kakao.com");
        when(socialSignupSessionRepository.consume("signup-token")).thenReturn(Optional.of(session));
        when(memberRepository.existsByNickname("닉네임")).thenReturn(false);
        when(memberRepository.existsByEmail("test@kakao.com")).thenReturn(false);
        when(tokenService.issueTokens(any())).thenReturn(new TokenResponse("access", "refresh"));

        TokenResponse response = socialAuthService.completeSignUp("signup-token", "닉네임", true);

        assertEquals("access", response.accessToken());
    }

    @Test
    void 만료되거나_잘못된_signupToken이면_AUTH_011_예외를_던진다() {
        when(socialSignupSessionRepository.consume("bad-token")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> socialAuthService.completeSignUp("bad-token", "닉네임", true));

        assertEquals(ErrorCode.AUTH_011, exception.getErrorCode());
    }

    @Test
    void 회원가입_완료_시_닉네임이_중복되면_AUTH_007_예외를_던진다() {
        SocialSignupSession session = new SocialSignupSession(AuthProvider.KAKAO, "12345", "test@kakao.com");
        when(socialSignupSessionRepository.consume("signup-token")).thenReturn(Optional.of(session));
        when(memberRepository.existsByNickname("닉네임")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> socialAuthService.completeSignUp("signup-token", "닉네임", true));

        assertEquals(ErrorCode.AUTH_007, exception.getErrorCode());
    }

    private void setId(Member member, Long id) {
        try {
            Field field = Member.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
