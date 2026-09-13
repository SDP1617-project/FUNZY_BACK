package com.sdp1617.backend.mypage.service;

import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.global.s3.S3ImageService;
import com.sdp1617.backend.mypage.dto.NicknameUpdateRequest;
import com.sdp1617.backend.mypage.dto.ProfileImagePresignedUrlRequest;
import com.sdp1617.backend.mypage.dto.ProfileImagePresignedUrlResponse;
import com.sdp1617.backend.mypage.dto.ProfileImageUploadCompleteRequest;
import com.sdp1617.backend.mypage.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final MemberRepository memberRepository;
    private final S3ImageService s3ImageService;

    public ProfileResponse getProfile(Long memberId) {
        Member member = findMember(memberId);
        return new ProfileResponse(member.getNickname(), member.getProfileImageUrl(), member.getProvider());
    }

    @Transactional
    public void updateNickname(Long memberId, NicknameUpdateRequest request) {
        Member member = findMember(memberId);
        if (request.nickname().equals(member.getNickname())) {
            return;
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.AUTH_007);
        }
        member.updateNickname(request.nickname());
    }

    public ProfileImagePresignedUrlResponse issueProfileImagePresignedUrl(
            Long memberId,
            ProfileImagePresignedUrlRequest request
    ) {
        S3ImageService.PresignedUpload upload = s3ImageService.issuePresignedUpload(
                imageKeyPrefix(memberId), request.contentType(), ErrorCode.MYPAGE_002);
        return ProfileImagePresignedUrlResponse.from(upload);
    }

    @Transactional
    public ProfileResponse completeProfileImageUpload(Long memberId, ProfileImageUploadCompleteRequest request) {
        s3ImageService.validateOwnership(request.imageKey(), imageKeyPrefix(memberId));
        s3ImageService.validateUploadedImage(
                request.imageKey(), ErrorCode.MYPAGE_001, ErrorCode.MYPAGE_002, ErrorCode.MYPAGE_003);

        Member member = findMember(memberId);
        String previousImageKey = member.getProfileImageKey();
        member.updateProfileImage(request.imageKey(), s3ImageService.buildImageUrl(request.imageKey()));
        if (previousImageKey != null && !previousImageKey.equals(request.imageKey())) {
            deleteAfterCommit(previousImageKey);
        }
        return new ProfileResponse(member.getNickname(), member.getProfileImageUrl(), member.getProvider());
    }

    @Transactional
    public void resetProfileImage(Long memberId) {
        Member member = findMember(memberId);
        String previousImageKey = member.getProfileImageKey();
        member.resetProfileImage();
        deleteAfterCommit(previousImageKey);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));
    }

    private String imageKeyPrefix(Long memberId) {
        return "profiles/" + memberId + "/";
    }

    /**
     * DB 트랜잭션 커밋 전에 S3 객체를 지우면, 이후 커밋 실패 시 DB는 롤백되는데
     * 이미 지워진 이미지를 계속 참조하게 된다. 커밋이 실제로 끝난 뒤에만 지우도록 미룬다.
     * (트랜잭션이 없는 컨텍스트, 예: 테스트에서는 즉시 삭제로 폴백)
     */
    private void deleteAfterCommit(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            s3ImageService.deleteImageQuietly(imageKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                s3ImageService.deleteImageQuietly(imageKey);
            }
        });
    }
}
