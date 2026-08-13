package com.sdp1617.backend.auth.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberTest {

    @Test
    void 실패횟수가_5회_미만이면_잠기지_않는다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);

        for (int i = 0; i < 4; i++) {
            member.increaseFailedLoginCount();
        }

        assertFalse(member.isLocked());
    }

    @Test
    void 실패횟수가_5회면_잠긴다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);

        for (int i = 0; i < 5; i++) {
            member.increaseFailedLoginCount();
        }

        assertTrue(member.isLocked());
    }

    @Test
    void unlock하면_실패횟수가_초기화되고_잠금이_풀린다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);
        for (int i = 0; i < 5; i++) {
            member.increaseFailedLoginCount();
        }

        member.unlock();

        assertFalse(member.isLocked());
        assertEquals(0, member.getFailedLoginCount());
    }

    @Test
    void resetFailedLoginCount하면_잠금상태도_함께_풀린다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);
        for (int i = 0; i < 5; i++) {
            member.increaseFailedLoginCount();
        }

        member.resetFailedLoginCount();

        assertFalse(member.isLocked());
    }

    @Test
    void 이메일_가입_회원은_비밀번호를_가진다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);

        assertTrue(member.hasPassword());
    }

    @Test
    void 소셜_가입_회원은_비밀번호가_없다() {
        Member member = new Member("test@kakao.com", "닉네임", true, AuthProvider.KAKAO, "12345");

        assertFalse(member.hasPassword());
        assertEquals(AuthProvider.KAKAO, member.getProvider());
        assertEquals("12345", member.getProviderId());
    }

    @Test
    void 소셜_가입_회원은_providerId가_없으면_예외를_던진다() {
        assertThrows(IllegalArgumentException.class,
                () -> new Member("test@kakao.com", "닉네임", true, AuthProvider.KAKAO, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Member("test@kakao.com", "닉네임", true, AuthProvider.KAKAO, ""));
    }

    @Test
    void 소셜_가입_생성자에_LOCAL_provider를_넘기면_예외를_던진다() {
        assertThrows(IllegalArgumentException.class,
                () -> new Member("test@sdp1617.com", "닉네임", true, AuthProvider.LOCAL, "12345"));
    }

    @Test
    void 팔로우코드를_재발급하면_값이_바뀐다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);
        member.reissueFollowCode();
        String firstCode = member.getFollowCode();

        member.reissueFollowCode();

        assertNotEquals(firstCode, member.getFollowCode());
    }

    @Test
    void 가입시_푸시_알림_수신은_기본으로_켜져있다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);

        assertTrue(member.isPushNotificationEnabled());
    }

    @Test
    void 푸시_알림_수신_설정을_변경할_수_있다() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", true);

        member.updatePushNotificationEnabled(false);

        assertFalse(member.isPushNotificationEnabled());
    }
}
