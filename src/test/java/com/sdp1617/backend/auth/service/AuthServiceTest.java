package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.LoginRequest;
import com.sdp1617.backend.auth.dto.SignUpRequest;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.email.VerificationLinkIssuedEvent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.repository.VerificationTokenRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private LoginAttemptRecorder loginAttemptRecorder;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        Field field = AuthService.class.getDeclaredField("frontendUrl");
        field.setAccessible(true);
        field.set(authService, "http://localhost:3000");
    }

    private Member member(String email, String password, String nickname) {
        return new Member(email, password, nickname, true);
    }

    @Test
    void 이메일이_중복되면_AUTH_006_예외를_던진다() {
        when(memberRepository.existsByEmail("test@sdp1617.com")).thenReturn(true);

        SignUpRequest request = new SignUpRequest("test@sdp1617.com", "Password1!", "Password1!", "닉네임", true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.signUp(request));

        assertEquals(ErrorCode.AUTH_006, exception.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void 닉네임이_중복되면_AUTH_007_예외를_던진다() {
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname("닉네임")).thenReturn(true);

        SignUpRequest request = new SignUpRequest("test@sdp1617.com", "Password1!", "Password1!", "닉네임", true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.signUp(request));

        assertEquals(ErrorCode.AUTH_007, exception.getErrorCode());
    }

    @Test
    void 비밀번호와_비밀번호확인이_다르면_AUTH_008_예외를_던진다() {
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);

        SignUpRequest request = new SignUpRequest("test@sdp1617.com", "Password1!", "Password2!", "닉네임", true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.signUp(request));

        assertEquals(ErrorCode.AUTH_008, exception.getErrorCode());
    }

    @Test
    void 정상_회원가입시_비밀번호를_암호화해서_저장한다() {
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");

        SignUpRequest request = new SignUpRequest("test@sdp1617.com", "Password1!", "Password1!", "닉네임", true);
        authService.signUp(request);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertEquals("encoded-password", captor.getValue().getPassword());
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_AUTH_001_예외를_던진다() {
        when(memberRepository.findByEmail("nobody@sdp1617.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nobody@sdp1617.com", "Password1!");

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        assertEquals(ErrorCode.AUTH_001, exception.getErrorCode());
    }

    @Test
    void 잠긴_계정으로_로그인하면_AUTH_010_예외를_던진다() {
        Member member = member("test@sdp1617.com", "encoded", "닉네임");
        for (int i = 0; i < 5; i++) {
            member.increaseFailedLoginCount();
        }
        when(memberRepository.findByEmail("test@sdp1617.com")).thenReturn(Optional.of(member));

        LoginRequest request = new LoginRequest("test@sdp1617.com", "Password1!");

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        assertEquals(ErrorCode.AUTH_010, exception.getErrorCode());
    }

    @Test
    void 비밀번호가_틀리면_실패기록을_위임하고_AUTH_001_예외를_던진다() {
        Member member = member("test@sdp1617.com", "encoded", "닉네임");
        setId(member, 1L);
        when(memberRepository.findByEmail("test@sdp1617.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        LoginRequest request = new LoginRequest("test@sdp1617.com", "wrong");

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        assertEquals(ErrorCode.AUTH_001, exception.getErrorCode());
        verify(loginAttemptRecorder).recordFailure(1L);
    }

    @Test
    void 로그인_성공시_실패횟수를_초기화하고_토큰을_발급한다() {
        Member member = member("test@sdp1617.com", "encoded", "닉네임");
        member.increaseFailedLoginCount();
        setId(member, 1L);
        when(memberRepository.findByEmail("test@sdp1617.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("Password1!", "encoded")).thenReturn(true);
        when(tokenService.issueTokens(1L)).thenReturn(new TokenResponse("access", "refresh"));

        LoginRequest request = new LoginRequest("test@sdp1617.com", "Password1!");
        TokenResponse response = authService.login(request);

        assertEquals("access", response.accessToken());
        assertEquals(0, member.getFailedLoginCount());
    }

    @Test
    void 유효한_토큰으로_비밀번호를_재설정하면_잠금도_풀리고_모든_세션이_폐기된다() {
        Member member = member("test@sdp1617.com", "old-encoded", "닉네임");
        for (int i = 0; i < 5; i++) {
            member.increaseFailedLoginCount();
        }
        when(verificationTokenRepository.consume("password-reset", "token-value")).thenReturn(Optional.of(1L));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("new-encoded");

        authService.resetPassword("token-value", "NewPassword1!", "NewPassword1!");

        assertEquals("new-encoded", member.getPassword());
        assertFalse(member.isLocked());
        verify(tokenService).revokeAllSessions(1L);
    }

    @Test
    void 만료되거나_잘못된_토큰으로_비밀번호_재설정시_AUTH_011_예외를_던진다() {
        when(verificationTokenRepository.consume("password-reset", "bad-token")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> authService.resetPassword("bad-token", "NewPassword1!", "NewPassword1!"));

        assertEquals(ErrorCode.AUTH_011, exception.getErrorCode());
    }

    @Test
    void 비밀번호_재설정_요청시_링크가_포함된_이벤트를_발행한다() {
        Member member = member("test@sdp1617.com", "encoded", "닉네임");
        setId(member, 1L);
        when(memberRepository.findByEmail("test@sdp1617.com")).thenReturn(Optional.of(member));
        when(verificationTokenRepository.issue("password-reset", 1L, Duration.ofMinutes(15))).thenReturn("token-value");

        authService.requestPasswordReset("test@sdp1617.com");

        ArgumentCaptor<VerificationLinkIssuedEvent> captor = ArgumentCaptor.forClass(VerificationLinkIssuedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals("test@sdp1617.com", captor.getValue().to());
        assertTrue(captor.getValue().body().contains("http://localhost:3000/reset-password?token=token-value"));
    }

    @Test
    void 비밀번호_재설정_요청시_존재하지_않는_이메일이면_조용히_무시한다() {
        when(memberRepository.findByEmail("nobody@sdp1617.com")).thenReturn(Optional.empty());

        authService.requestPasswordReset("nobody@sdp1617.com");

        verify(eventPublisher, never()).publishEvent(any());
        verify(verificationTokenRepository, never()).issue(any(), any(), any());
    }

    @Test
    void 계정_잠금_해제_요청시_존재하지_않는_이메일이면_조용히_무시한다() {
        when(memberRepository.findByEmail("nobody@sdp1617.com")).thenReturn(Optional.empty());

        authService.requestAccountUnlock("nobody@sdp1617.com");

        verify(eventPublisher, never()).publishEvent(any());
        verify(verificationTokenRepository, never()).issue(any(), any(), any());
    }

    @Test
    void 계정_잠금_해제시_실패횟수를_초기화하고_모든_세션을_폐기한다() {
        Member member = member("test@sdp1617.com", "encoded", "닉네임");
        for (int i = 0; i < 5; i++) {
            member.increaseFailedLoginCount();
        }
        when(verificationTokenRepository.consume("account-unlock", "token-value")).thenReturn(Optional.of(1L));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        authService.unlockAccount("token-value");

        assertFalse(member.isLocked());
        assertEquals(0, member.getFailedLoginCount());
        verify(tokenService).revokeAllSessions(1L);
    }

    @Test
    void 사용_가능한_닉네임이면_true를_반환한다() {
        when(memberRepository.existsByNickname("새닉네임")).thenReturn(false);

        assertTrue(authService.isNicknameAvailable("새닉네임"));
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
