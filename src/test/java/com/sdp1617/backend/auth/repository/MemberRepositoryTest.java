package com.sdp1617.backend.auth.repository;

import com.sdp1617.backend.auth.entity.Consent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * existsByNickname 체크 후 저장하는 흐름은 동시 요청 사이 경쟁 상태(race condition)를 완전히 막지 못해
 * 최종 방어선인 DB 유니크 제약 위반(DataIntegrityViolationException)으로 이어질 수 있다.
 * 실제 동시 요청은 Mockito로 재현하기 어려우므로, flush 시점에 제약 위반이 발생했다고 가정하고
 * saveWithNicknameUniqueness가 이를 AUTH_007로 정확히 매핑하는지만 검증한다.
 */
class MemberRepositoryTest {

    private Member member() {
        return new Member("test@sdp1617.com", "encoded", "닉네임", Consent.requiredOnly());
    }

    @Test
    void 닉네임_유니크_제약_위반이면_AUTH_007_예외로_변환한다() {
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class, Answers.CALLS_REAL_METHODS);
        Member member = member();
        when(memberRepository.saveAndFlush(member))
                .thenThrow(dataIntegrityViolation("uk_member_nickname"));

        CustomException exception = assertThrows(CustomException.class,
                () -> memberRepository.saveWithNicknameUniqueness(member));

        assertEquals(ErrorCode.AUTH_007, exception.getErrorCode());
    }

    @Test
    void 닉네임이_아닌_다른_유니크_제약_위반이면_원래_예외를_그대로_던진다() {
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class, Answers.CALLS_REAL_METHODS);
        Member member = member();
        DataIntegrityViolationException emailViolation = dataIntegrityViolation("uk_member_email");
        when(memberRepository.saveAndFlush(member)).thenThrow(emailViolation);

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () -> memberRepository.saveWithNicknameUniqueness(member));

        assertSame(emailViolation, exception);
    }

    private DataIntegrityViolationException dataIntegrityViolation(String constraintName) {
        ConstraintViolationException cause = new ConstraintViolationException(
                "constraint violated", new SQLException("duplicate key"), constraintName);
        return new DataIntegrityViolationException("unique constraint", cause);
    }

    @Test
    void 저장에_성공하면_저장된_회원을_그대로_반환한다() {
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class, Answers.CALLS_REAL_METHODS);
        Member member = member();
        when(memberRepository.saveAndFlush(member)).thenReturn(member);

        Member saved = memberRepository.saveWithNicknameUniqueness(member);

        assertSame(member, saved);
    }
}
