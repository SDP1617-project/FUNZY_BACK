package com.sdp1617.backend.auth.entity;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SocialConnectionTest {

    private Member member() {
        Member member = new Member("test@sdp1617.com", "encoded", "닉네임", Consent.requiredOnly());
        try {
            Field field = Member.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(member, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return member;
    }

    @Test
    void 소셜_연결을_생성한다() {
        Member member = member();

        SocialConnection connection = SocialConnection.create(member, AuthProvider.KAKAO, "12345");

        assertSame(member, connection.getMember());
        assertEquals(AuthProvider.KAKAO, connection.getProvider());
        assertEquals("12345", connection.getProviderId());
    }

    @Test
    void LOCAL_provider로는_생성할_수_없다() {
        Member member = member();

        assertThrows(IllegalArgumentException.class,
                () -> SocialConnection.create(member, AuthProvider.LOCAL, "12345"));
    }
}
