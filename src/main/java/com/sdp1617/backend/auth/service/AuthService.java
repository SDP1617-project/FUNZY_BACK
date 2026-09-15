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
    private static final String EMAIL_VERIFICATION_PURPOSE = "email-verification";
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
                request.toConsent()
        );
        memberRepository.saveWithNicknameUniqueness(member);
        sendVerificationEmail(member);
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

        if (!member.isEmailVerified()) {
            throw new CustomException(ErrorCode.AUTH_016);
        }

        member.resetFailedLoginCount();
        return tokenService.issueTokens(member.getId());
    }

    @Transactional
    public void resendEmailVerification(String email) {
        // 존재하지 않는 이메일이거나 이미 인증된 계정(소셜 포함)이면 조용히 무시 (계정 존재 여부 비노출)
        memberRepository.findByEmail(email)
                .filter(member -> !member.isEmailVerified())
                .ifPresent(this::sendVerificationEmail);
    }

    @Transactional
    public void verifyEmail(String token) {
        Long memberId = verificationTokenRepository.consume(EMAIL_VERIFICATION_PURPOSE, token)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_011));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));

        member.verifyEmail();
    }

    private void sendVerificationEmail(Member member) {
        String token = verificationTokenRepository.issue(EMAIL_VERIFICATION_PURPOSE, member.getId(), VERIFICATION_TOKEN_TTL);
        String link = frontendUrl + "/verify-email?token=" + token;
        eventPublisher.publishEvent(new VerificationLinkIssuedEvent(
                member.getEmail(),
                "이메일 인증 안내",
                "아래 링크에서 이메일 인증을 완료해주세요 (15분간 유효):\n" + link
        ));
    }

    @Transactional
    public void requestPasswordReset(String email) {
        // 소셜 전용 계정은 비밀번호가 없으므로 재설정 대상에서 제외 (계정 존재 여부가 드러나지 않도록 조용히 무시)
        memberRepository.findByEmail(email)
                .filter(Member::hasPassword)
                .ifPresent(member -> {
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

        if (!member.hasPassword()) {
            throw new CustomException(ErrorCode.AUTH_011);
        }

        member.changePassword(passwordEncoder.encode(newPassword));
        member.unlock();
        eventPublisher.publishEvent(new AllSessionsRevokedEvent(memberId));
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
        eventPublisher.publishEvent(new AllSessionsRevokedEvent(memberId));
    }
}
