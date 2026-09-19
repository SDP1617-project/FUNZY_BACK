package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.SocialAuthResponse;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Consent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.entity.SocialConnection;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.repository.SocialConnectionRepository;
import com.sdp1617.backend.auth.repository.SocialSignupSessionRepository;
import com.sdp1617.backend.auth.social.SocialSignupSession;
import com.sdp1617.backend.auth.social.SocialUserInfo;
import com.sdp1617.backend.auth.social.SocialUserInfoProvider;
import com.sdp1617.backend.auth.social.SocialUserInfoProviderRegistry;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthService {

    private static final Duration SIGNUP_SESSION_TTL = Duration.ofMinutes(15);

    private final SocialUserInfoProviderRegistry providerRegistry;
    private final MemberRepository memberRepository;
    private final SocialConnectionRepository socialConnectionRepository;
    private final SocialSignupSessionRepository socialSignupSessionRepository;
    private final TokenService tokenService;

    /**
     * 소셜 로그인 여부 판단은 (이제 회원당 하나만 표현 가능한) Member.provider/providerId가 아니라,
     * 회원이 여러 소셜 수단을 동시에 연결할 수 있는 SocialConnection을 기준으로 한다.
     */
    public SocialAuthResponse login(AuthProvider provider, String token) {
        SocialUserInfoProvider userInfoProvider = providerRegistry.get(provider);
        SocialUserInfo userInfo = userInfoProvider.fetchUserInfo(token);

        return socialConnectionRepository.findByProviderAndProviderId(provider, userInfo.externalId())
                .map(connection -> SocialAuthResponse.existingUser(tokenService.issueTokens(connection.getMember().getId())))
                .orElseGet(() -> startSignup(provider, userInfo));
    }

    private SocialAuthResponse startSignup(AuthProvider provider, SocialUserInfo userInfo) {
        if (userInfo.email() != null && memberRepository.existsByEmail(userInfo.email())) {
            throw new CustomException(ErrorCode.AUTH_012);
        }

        SocialSignupSession session = new SocialSignupSession(provider, userInfo.externalId(), userInfo.email());
        String signupToken = socialSignupSessionRepository.issue(session, SIGNUP_SESSION_TTL);
        return SocialAuthResponse.newUser(signupToken, userInfo.email());
    }

    @Transactional
    public TokenResponse completeSignUp(String signupToken, String nickname, Consent consent) {
        SocialSignupSession session = socialSignupSessionRepository.consume(signupToken)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_011));

        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.AUTH_007);
        }
        if (session.email() != null && memberRepository.existsByEmail(session.email())) {
            throw new CustomException(ErrorCode.AUTH_012);
        }

        try {
            Member member = new Member(session.email(), nickname, consent, session.provider(), session.externalId());
            memberRepository.saveWithNicknameUniqueness(member);
            // saveAndFlush로 커밋을 기다리지 않고 바로 제약 위반을 드러낸다 — save만 쓰면 위반이
            // 트랜잭션 커밋 시점(메서드 반환 이후)에야 터져서, 아래 issueTokens가 이미 실행된 뒤
            // 회원가입 자체는 롤백되는 모순된 상태가 될 수 있다.
            socialConnectionRepository.saveAndFlush(
                    SocialConnection.create(member, session.provider(), session.externalId()));
            return tokenService.issueTokens(member.getId());
        } catch (DataIntegrityViolationException exception) {
            if (!violatesProviderConnectionConstraint(exception)) {
                throw exception;
            }
            // 같은 소셜 계정으로 동시에 signup을 완료하려던 경쟁에서 진 쪽 — 이미 다른 요청이
            // 먼저 계정을 만들었을 것이므로, 에러 대신 그 계정으로 로그인시켜준다.
            return socialConnectionRepository.findByProviderAndProviderId(session.provider(), session.externalId())
                    .map(connection -> tokenService.issueTokens(connection.getMember().getId()))
                    .orElseThrow(() -> exception);
        }
    }

    private boolean violatesProviderConnectionConstraint(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = constraintViolationException.getConstraintName();
                return "uk_member_provider_provider_id".equalsIgnoreCase(constraintName)
                        || "uk_social_connection_provider_provider_id".equalsIgnoreCase(constraintName);
            }
        }
        return false;
    }
}
