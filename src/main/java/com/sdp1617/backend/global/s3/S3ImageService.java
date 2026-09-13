package com.sdp1617.backend.global.s3;

import com.sdp1617.backend.global.config.properties.S3Properties;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

/**
 * 마음카드/프로필 등 여러 도메인이 공유하는 S3 이미지 업로드(presigned URL 발급 + 업로드 완료 검증) 로직.
 * 각 도메인은 자신의 키 prefix와 실패 시 사용할 ErrorCode만 넘기면 된다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class S3ImageService {

    private static final int IMAGE_SIGNATURE_READ_BYTES = 16;
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public record PresignedUpload(
            String uploadUrl,
            String imageKey,
            String imageUrl,
            String method,
            Instant expiresAt
    ) {
    }

    public PresignedUpload issuePresignedUpload(String keyPrefix, String contentType, ErrorCode invalidFormatError) {
        String normalizedContentType = normalizeContentType(contentType, invalidFormatError);
        validateContentType(normalizedContentType, invalidFormatError);

        String imageKey = keyPrefix + UUID.randomUUID() + extensionFor(normalizedContentType, invalidFormatError);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .contentType(normalizedContentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUpload(
                presignedRequest.url().toString(),
                imageKey,
                buildImageUrl(imageKey),
                "PUT",
                expiresAt
        );
    }

    public void validateOwnership(String imageKey, String keyPrefix) {
        if (!imageKey.startsWith(keyPrefix)) {
            throw new CustomException(ErrorCode.COMMON_004);
        }
    }

    public void validateUploadedImage(
            String imageKey,
            ErrorCode notFoundError,
            ErrorCode invalidFormatError,
            ErrorCode tooLargeError
    ) {
        HeadObjectResponse objectMetadata = getImageObjectMetadata(imageKey, notFoundError);
        String contentType = normalizeContentType(objectMetadata.contentType(), invalidFormatError);
        validateContentType(contentType, invalidFormatError);
        validateImageSize(objectMetadata.contentLength(), invalidFormatError, tooLargeError);
        validateImageSignature(imageKey, contentType, notFoundError, invalidFormatError);
    }

    public String buildImageUrl(String imageKey) {
        if (s3Properties.publicBaseUrl() != null && !s3Properties.publicBaseUrl().isBlank()) {
            return s3Properties.publicBaseUrl().replaceAll("/$", "") + "/" + imageKey;
        }
        return "https://" + s3Properties.bucket() + ".s3." + s3Properties.staticRegion() + ".amazonaws.com/" + imageKey;
    }

    /**
     * 이전 이미지 정리용. 실패해도 주 흐름(이미지 교체/초기화)을 막지 않도록 예외를 삼키고 로그만 남긴다.
     */
    public void deleteImageQuietly(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(imageKey)
                    .build());
        } catch (SdkException exception) {
            log.warn("이전 S3 이미지 삭제 실패: key={}", imageKey, exception);
        }
    }

    private HeadObjectResponse getImageObjectMetadata(String imageKey, ErrorCode notFoundError) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(imageKey)
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new CustomException(notFoundError);
            }
            throw exception;
        }
    }

    private void validateImageSize(Long contentLength, ErrorCode invalidFormatError, ErrorCode tooLargeError) {
        if (contentLength == null || contentLength <= 0) {
            throw new CustomException(invalidFormatError);
        }
        if (contentLength > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(tooLargeError);
        }
    }

    private void validateImageSignature(
            String imageKey,
            String contentType,
            ErrorCode notFoundError,
            ErrorCode invalidFormatError
    ) {
        byte[] signature = readImageSignature(imageKey, notFoundError, invalidFormatError);
        boolean valid = switch (contentType) {
            case "image/jpeg" -> isJpeg(signature);
            case "image/png" -> isPng(signature);
            case "image/webp" -> isWebp(signature);
            default -> false;
        };
        if (!valid) {
            throw new CustomException(invalidFormatError);
        }
    }

    private byte[] readImageSignature(String imageKey, ErrorCode notFoundError, ErrorCode invalidFormatError) {
        try (ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .range("bytes=0-" + (IMAGE_SIGNATURE_READ_BYTES - 1))
                .build())) {
            return object.readNBytes(IMAGE_SIGNATURE_READ_BYTES);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new CustomException(notFoundError);
            }
            throw exception;
        } catch (IOException exception) {
            throw new CustomException(invalidFormatError);
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

    private void validateContentType(String contentType, ErrorCode invalidFormatError) {
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(invalidFormatError);
        }
    }

    private String extensionFor(String contentType, ErrorCode invalidFormatError) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new CustomException(invalidFormatError);
        };
    }

    private String normalizeContentType(String contentType, ErrorCode invalidFormatError) {
        if (contentType == null) {
            throw new CustomException(invalidFormatError);
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }
}
