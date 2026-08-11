package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptRecorderTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LoginAttemptRecorder loginAttemptRecorder;

    @Test
    void 존재하는_회원의_실패횟수를_증가시킨다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        loginAttemptRecorder.recordFailure(1L);

        assertEquals(1, member.getFailedLoginCount());
    }

    @Test
    void 존재하지_않는_회원이면_AUTH_002_예외를_던진다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> loginAttemptRecorder.recordFailure(1L));

        assertEquals(ErrorCode.AUTH_002, exception.getErrorCode());
    }
}
