package com.sdp1617.backend.mypage.service;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.auth.service.TokenService;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.mypage.dto.ConnectedAccountResponse;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSettingsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AccountSettingsService accountSettingsService;

    private Member localMember() {
        return new Member("test@sdp1617.com", "encoded", "닉네임", true);
    }

    private Member socialMember() {
        return new Member("social@sdp1617.com", "닉네임", true, AuthProvider.KAKAO, "12345");
    }

    private void setId(Member member, Long id) {
        try {
            Field field = Member.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 연결_계정을_조회한다() {
        Member member = socialMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        ConnectedAccountResponse response = accountSettingsService.getConnectedAccount(1L);

        assertEquals(AuthProvider.KAKAO, response.provider());
        assertFalse(response.hasPassword());
    }

    @Test
    void 비밀번호_변경에_성공하면_모든_세션을_폐기한다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("oldPw1!", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("new-encoded");

        accountSettingsService.changePassword(1L, "oldPw1!", "NewPassword1!", "NewPassword1!");

        assertEquals("new-encoded", member.getPassword());
        verify(tokenService).revokeAllSessions(1L);
    }

    @Test
    void 소셜_전용_계정은_비밀번호_변경시_AUTH_014_예외를_던진다() {
        Member member = socialMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        CustomException exception = assertThrows(CustomException.class,
                () -> accountSettingsService.changePassword(1L, "oldPw1!", "NewPassword1!", "NewPassword1!"));

        assertEquals(ErrorCode.AUTH_014, exception.getErrorCode());
    }

    @Test
    void 현재_비밀번호가_틀리면_AUTH_015_예외를_던진다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class,
                () -> accountSettingsService.changePassword(1L, "wrong", "NewPassword1!", "NewPassword1!"));

        assertEquals(ErrorCode.AUTH_015, exception.getErrorCode());
    }

    @Test
    void 새_비밀번호와_확인이_다르면_AUTH_008_예외를_던진다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("oldPw1!", "encoded")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> accountSettingsService.changePassword(1L, "oldPw1!", "NewPassword1!", "Different1!"));

        assertEquals(ErrorCode.AUTH_008, exception.getErrorCode());
    }

    @Test
    void 로그아웃시_해당_세션만_폐기한다() {
        accountSettingsService.logout(1L, "refresh-token");

        verify(tokenService).revokeSession(1L, "refresh-token");
    }

    @Test
    void 회원_탈퇴시_계정을_삭제하고_모든_세션을_폐기한다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        accountSettingsService.withdraw(1L);

        verify(memberRepository).delete(member);
        verify(tokenService).revokeAllSessions(1L);
    }

    @Test
    void 존재하지_않는_회원이면_AUTH_002_예외를_던진다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> accountSettingsService.withdraw(1L));

        assertEquals(ErrorCode.AUTH_002, exception.getErrorCode());
    }
}
