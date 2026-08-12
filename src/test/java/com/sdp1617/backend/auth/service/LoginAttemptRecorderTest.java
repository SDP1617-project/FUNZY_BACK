package com.sdp1617.backend.auth.service;

import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptRecorderTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LoginAttemptRecorder loginAttemptRecorder;

    @Test
    void 존재하는_회원이면_실패횟수를_원자적으로_증가시킨다() {
        when(memberRepository.incrementFailedLoginCount(1L)).thenReturn(1);

        loginAttemptRecorder.recordFailure(1L);

        verify(memberRepository).incrementFailedLoginCount(1L);
    }

    @Test
    void 존재하지_않는_회원이면_AUTH_002_예외를_던진다() {
        when(memberRepository.incrementFailedLoginCount(1L)).thenReturn(0);

        CustomException exception = assertThrows(CustomException.class, () -> loginAttemptRecorder.recordFailure(1L));

        assertEquals(ErrorCode.AUTH_002, exception.getErrorCode());
    }
}
