package com.sdp1617.backend.mypage.service;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.auth.entity.Consent;
import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.global.config.properties.S3Properties;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.mypage.dto.NicknameUpdateRequest;
import com.sdp1617.backend.mypage.dto.ProfileImagePresignedUrlRequest;
import com.sdp1617.backend.mypage.dto.ProfileImageUploadCompleteRequest;
import com.sdp1617.backend.mypage.dto.ProfileResponse;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private final S3Properties s3Properties = new S3Properties(
            new S3Properties.Credentials(null, null),
            new S3Properties.Region("ap-northeast-2"),
            new S3Properties.S3("sdp-funzy", null, 10),
            new S3Properties.Stack(false)
    );

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(memberRepository, s3Client, s3Presigner, s3Properties);
    }

    private Member localMember() {
        return new Member("test@sdp1617.com", "encoded", "닉네임", Consent.requiredOnly());
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
    void 프로필을_조회한다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        ProfileResponse response = profileService.getProfile(1L);

        assertEquals("닉네임", response.nickname());
        assertNull(response.profileImageUrl());
        assertEquals(AuthProvider.LOCAL, response.provider());
    }

    @Test
    void 존재하지_않는_회원_조회시_AUTH_002_예외를_던진다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> profileService.getProfile(1L));

        assertEquals(ErrorCode.AUTH_002, exception.getErrorCode());
    }

    @Test
    void 닉네임을_변경한다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname("새닉네임")).thenReturn(false);

        profileService.updateNickname(1L, new NicknameUpdateRequest("새닉네임"));

        assertEquals("새닉네임", member.getNickname());
    }

    @Test
    void 기존_닉네임과_동일하면_중복_검사_없이_통과한다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        profileService.updateNickname(1L, new NicknameUpdateRequest("닉네임"));

        assertEquals("닉네임", member.getNickname());
    }

    @Test
    void 다른_회원이_사용중인_닉네임이면_AUTH_007_예외를_던진다() {
        Member member = localMember();
        setId(member, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname("중복닉네임")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> profileService.updateNickname(1L, new NicknameUpdateRequest("중복닉네임")));

        assertEquals(ErrorCode.AUTH_007, exception.getErrorCode());
    }

    @Test
    void 프로필_이미지를_초기화한다() {
        Member member = localMember();
        setId(member, 1L);
        member.updateProfileImage("profiles/1/old.png", "https://example.com/profiles/1/old.png");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        profileService.resetProfileImage(1L);

        assertNull(member.getProfileImageKey());
        assertNull(member.getProfileImageUrl());
    }

    @Test
    void 지원하지_않는_이미지_형식이면_presigned_URL_발급시_MYPAGE_002_예외를_던진다() {
        CustomException exception = assertThrows(CustomException.class,
                () -> profileService.issueProfileImagePresignedUrl(
                        1L, new ProfileImagePresignedUrlRequest("virus.exe", "application/octet-stream")));

        assertEquals(ErrorCode.MYPAGE_002, exception.getErrorCode());
    }

    @Test
    void 본인_소유가_아닌_이미지_키로_업로드_완료를_요청하면_COMMON_004_예외를_던지고_S3를_조회하지_않는다() {
        CustomException exception = assertThrows(CustomException.class,
                () -> profileService.completeProfileImageUpload(
                        1L, new ProfileImageUploadCompleteRequest("profiles/999/other.png")));

        assertEquals(ErrorCode.COMMON_004, exception.getErrorCode());
        verifyNoInteractions(s3Client);
    }
}
