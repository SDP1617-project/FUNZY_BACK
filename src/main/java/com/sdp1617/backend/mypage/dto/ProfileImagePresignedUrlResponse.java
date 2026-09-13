package com.sdp1617.backend.mypage.dto;

import com.sdp1617.backend.global.s3.S3ImageService;

import java.time.Instant;

public record ProfileImagePresignedUrlResponse(
        String uploadUrl,
        String imageKey,
        String imageUrl,
        String method,
        Instant expiresAt
) {
    public static ProfileImagePresignedUrlResponse from(S3ImageService.PresignedUpload upload) {
        return new ProfileImagePresignedUrlResponse(
                upload.uploadUrl(),
                upload.imageKey(),
                upload.imageUrl(),
                upload.method(),
                upload.expiresAt()
        );
    }
}
