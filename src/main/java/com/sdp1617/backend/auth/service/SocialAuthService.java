package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.dto.SocialAuthResponse;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Consent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.repository.SocialSignupSessionRepository;
import com.sdp1617.backend.auth.social.SocialSignupSession;
import com.sdp1617.backend.auth.social.SocialUserInfo;
import com.sdp1617.backend.auth.social.SocialUserInfoProvider;
import com.sdp1617.backend.auth.social.SocialUserInfoProviderRegistry;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthService {

    private static final Duration SIGNUP_SESSION_TTL = Duration.ofMinutes(15);

    private final SocialUserInfoProviderRegistry providerRegistry;
    private final MemberRepository memberRepository;
    private final SocialSignupSessionRepository socialSignupSessionRepository;
    private final TokenService tokenService;

    public SocialAuthResponse login(AuthProvider provider, String token) {
        SocialUserInfoProvider userInfoProvider = providerRegistry.get(provider);
        SocialUserInfo userInfo = userInfoProvider.fetchUserInfo(token);

        return memberRepository.findByProviderAndProviderId(provider, userInfo.externalId())
                .map(member -> SocialAuthResponse.existingUser(tokenService.issueTokens(member.getId())))
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

        Member member = new Member(session.email(), nickname, consent, session.provider(), session.externalId());
        memberRepository.save(member);

        return tokenService.issueTokens(member.getId());
    }
}
