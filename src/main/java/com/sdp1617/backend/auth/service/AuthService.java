package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.LoginRequest;
import com.sdp1617.backend.auth.dto.SignUpRequest;
import com.sdp1617.backend.auth.email.VerificationLinkIssuedEvent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.repository.VerificationTokenRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String PASSWORD_RESET_PURPOSE = "password-reset";
    private static final String ACCOUNT_UNLOCK_PURPOSE = "account-unlock";
    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofMinutes(15);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptRecorder loginAttemptRecorder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.AUTH_006);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.AUTH_007);
        }
        if (!request.password().equals(request.passwordConfirm())) {
            throw new CustomException(ErrorCode.AUTH_008);
        }

        Member member = new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.termsAgreed()
        );
        memberRepository.save(member);
    }

    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_001));

        if (member.isLocked()) {
            throw new CustomException(ErrorCode.AUTH_010);
        }

        if (!member.hasPassword()) {
            // 소셜 전용 계정 - 계정 존재 여부가 드러나지 않도록 일반 로그인 실패와 동일하게 처리
            loginAttemptRecorder.recordFailure(member.getId());
            throw new CustomException(ErrorCode.AUTH_001);
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            loginAttemptRecorder.recordFailure(member.getId());
            throw new CustomException(ErrorCode.AUTH_001);
        }

        member.resetFailedLoginCount();
        return tokenService.issueTokens(member.getId());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            String token = verificationTokenRepository.issue(PASSWORD_RESET_PURPOSE, member.getId(), VERIFICATION_TOKEN_TTL);
            String link = frontendUrl + "/reset-password?token=" + token;
            eventPublisher.publishEvent(new VerificationLinkIssuedEvent(
                    member.getEmail(),
                    "비밀번호 재설정 안내",
                    "아래 링크에서 비밀번호를 재설정해주세요 (15분간 유효):\n" + link
            ));
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new CustomException(ErrorCode.AUTH_008);
        }

        Long memberId = verificationTokenRepository.consume(PASSWORD_RESET_PURPOSE, token)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_011));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));

        member.changePassword(passwordEncoder.encode(newPassword));
        member.unlock();
        tokenService.revokeAllSessions(memberId);
    }

    @Transactional
    public void requestAccountUnlock(String email) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            String token = verificationTokenRepository.issue(ACCOUNT_UNLOCK_PURPOSE, member.getId(), VERIFICATION_TOKEN_TTL);
            String link = frontendUrl + "/unlock-account?token=" + token;
            eventPublisher.publishEvent(new VerificationLinkIssuedEvent(
                    member.getEmail(),
                    "계정 잠금 해제 안내",
                    "아래 링크에서 계정 잠금을 해제해주세요 (15분간 유효):\n" + link
            ));
        });
    }

    @Transactional
    public void unlockAccount(String token) {
        Long memberId = verificationTokenRepository.consume(ACCOUNT_UNLOCK_PURPOSE, token)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_011));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));

        member.unlock();
        tokenService.revokeAllSessions(memberId);
    }
}
