package com.sdp1617.backend.mypage.service;

import com.sdp1617.backend.auth.entity.Member;
import com.sdp1617.backend.auth.repository.MemberRepository;
import com.sdp1617.backend.global.config.properties.S3Properties;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import com.sdp1617.backend.mypage.dto.NicknameUpdateRequest;
import com.sdp1617.backend.mypage.dto.ProfileImagePresignedUrlRequest;
import com.sdp1617.backend.mypage.dto.ProfileImagePresignedUrlResponse;
import com.sdp1617.backend.mypage.dto.ProfileImageUploadCompleteRequest;
import com.sdp1617.backend.mypage.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private static final int IMAGE_SIGNATURE_READ_BYTES = 16;
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

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
        String contentType = normalizeContentType(request.contentType());
        validateImageContentType(contentType);

        String imageKey = createImageKey(memberId, contentType);
        Instant expiresAt = Instant.now()
                .plus(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new ProfileImagePresignedUrlResponse(
                presignedRequest.url().toString(),
                imageKey,
                createImageUrl(imageKey),
                "PUT",
                expiresAt
        );
    }

    @Transactional
    public ProfileResponse completeProfileImageUpload(Long memberId, ProfileImageUploadCompleteRequest request) {
        validateImageKeyOwner(memberId, request.imageKey());
        validateUploadedImage(request.imageKey());

        Member member = findMember(memberId);
        member.updateProfileImage(request.imageKey(), createImageUrl(request.imageKey()));
        return new ProfileResponse(member.getNickname(), member.getProfileImageUrl(), member.getProvider());
    }

    @Transactional
    public void resetProfileImage(Long memberId) {
        Member member = findMember(memberId);
        member.resetProfileImage();
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_002));
    }

    private void validateImageContentType(String contentType) {
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.MYPAGE_002);
        }
    }

    private void validateUploadedImage(String imageKey) {
        HeadObjectResponse objectMetadata = getImageObjectMetadata(imageKey);
        String contentType = normalizeContentType(objectMetadata.contentType());
        validateImageContentType(contentType);
        validateImageSize(objectMetadata.contentLength());
        validateImageSignature(imageKey, contentType);
    }

    private HeadObjectResponse getImageObjectMetadata(String imageKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(imageKey)
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new CustomException(ErrorCode.MYPAGE_001);
            }
            throw exception;
        }
    }

    private void validateImageSize(Long contentLength) {
        if (contentLength == null || contentLength <= 0) {
            throw new CustomException(ErrorCode.MYPAGE_002);
        }
        if (contentLength > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(ErrorCode.MYPAGE_003);
        }
    }

    private void validateImageSignature(String imageKey, String contentType) {
        byte[] signature = readImageSignature(imageKey);
        boolean valid = switch (contentType) {
            case "image/jpeg" -> isJpeg(signature);
            case "image/png" -> isPng(signature);
            case "image/webp" -> isWebp(signature);
            default -> false;
        };
        if (!valid) {
            throw new CustomException(ErrorCode.MYPAGE_002);
        }
    }

    private byte[] readImageSignature(String imageKey) {
        try (ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .range("bytes=0-" + (IMAGE_SIGNATURE_READ_BYTES - 1))
                .build())) {
            return object.readNBytes(IMAGE_SIGNATURE_READ_BYTES);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new CustomException(ErrorCode.MYPAGE_001);
            }
            throw exception;
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.MYPAGE_002);
        }
    }

    private boolean isJpeg(byte[] signature) {
        return signature.length >= 3
                && (signature[0] & 0xFF) == 0xFF
                && (signature[1] & 0xFF) == 0xD8
                && (signature[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] signature) {
        int[] pngSignature = {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (signature.length < pngSignature.length) {
            return false;
        }
        for (int i = 0; i < pngSignature.length; i++) {
            if ((signature[i] & 0xFF) != pngSignature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] signature) {
        return signature.length >= 12
                && signature[0] == 'R'
                && signature[1] == 'I'
                && signature[2] == 'F'
                && signature[3] == 'F'
                && signature[8] == 'W'
                && signature[9] == 'E'
                && signature[10] == 'B'
                && signature[11] == 'P';
    }

    private void validateImageKeyOwner(Long memberId, String imageKey) {
        if (!imageKey.startsWith(imageKeyPrefix(memberId))) {
            throw new CustomException(ErrorCode.COMMON_004);
        }
    }

    private String createImageKey(Long memberId, String contentType) {
        return imageKeyPrefix(memberId) + UUID.randomUUID() + extensionFor(contentType);
    }

    private String imageKeyPrefix(Long memberId) {
        return "profiles/" + memberId + "/";
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new CustomException(ErrorCode.MYPAGE_002);
        };
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            throw new CustomException(ErrorCode.MYPAGE_002);
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private String createImageUrl(String imageKey) {
        if (s3Properties.publicBaseUrl() != null && !s3Properties.publicBaseUrl().isBlank()) {
            return s3Properties.publicBaseUrl().replaceAll("/$", "") + "/" + imageKey;
        }
        return "https://" + s3Properties.bucket() + ".s3." + s3Properties.staticRegion() + ".amazonaws.com/" + imageKey;
    }
}
