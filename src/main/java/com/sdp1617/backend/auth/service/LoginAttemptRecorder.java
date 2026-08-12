package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAttemptRecorder {

    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long memberId) {
        int updated = memberRepository.incrementFailedLoginCount(memberId);
        if (updated == 0) {
            throw new CustomException(ErrorCode.AUTH_002);
        }
    }
}
