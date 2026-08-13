package com.sdp1617.backend.mypage.service;

import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.service.AllSessionsRevokedEvent;
import com.sdp1617.backend.auth.service.TokenService;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.mypage.dto.ConnectedAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountSettingsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;

    public ConnectedAccountResponse getConnectedAccount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));

        return new ConnectedAccountResponse(member.getProvider(), member.hasPassword());
    }

    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword, String newPasswordConfirm) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));

        if (!member.hasPassword()) {
            throw new CustomException(ErrorCode.AUTH_014);
        }
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_015);
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new CustomException(ErrorCode.AUTH_008);
        }

        member.changePassword(passwordEncoder.encode(newPassword));
        eventPublisher.publishEvent(new AllSessionsRevokedEvent(memberId));
    }

    @Transactional
    public void logout(Long memberId, String refreshToken) {
        tokenService.revokeSession(memberId, refreshToken);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));

        memberRepository.delete(member);
        eventPublisher.publishEvent(new AllSessionsRevokedEvent(memberId));
    }
}
