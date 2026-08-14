package com.sdp1617.backend.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

@ConfigurationProperties(prefix = "cloud.aws")
public record S3Properties(
        Credentials credentials,
        Region region,
        S3 s3,
        Stack stack
) {
    public S3Properties {
        if (credentials == null) {
            credentials = new Credentials(null, null);
        }
        if (region == null) {
            region = new Region(null);
        }
        if (s3 == null) {
            s3 = new S3(null, null, 0);
        }
        if (stack == null) {
            stack = new Stack(false);
        }

        if (s3.bucket() == null || s3.bucket().isBlank()) {
            throw new IllegalStateException("cloud.aws.s3.bucket 설정이 필요합니다.");
        }
        if (region.staticRegion() == null || region.staticRegion().isBlank()) {
            throw new IllegalStateException("cloud.aws.region.static 설정이 필요합니다.");
        }
        if (s3.presignedUrlExpirationMinutes() <= 0) {
            throw new IllegalStateException("cloud.aws.s3.presigned-url-expiration-minutes 설정이 올바르지 않습니다.");
        }
    }

    public String bucket() {
        return s3.bucket();
    }

    public String staticRegion() {
        return region.staticRegion();
    }

    public String publicBaseUrl() {
        return s3.publicBaseUrl();
    }

    public long presignedUrlExpirationMinutes() {
        return s3.presignedUrlExpirationMinutes();
    }

    public boolean hasStaticCredentials() {
        return credentials.accessKey() != null
                && !credentials.accessKey().isBlank()
                && credentials.secretKey() != null
                && !credentials.secretKey().isBlank();
    }

    public String accessKey() {
        return credentials.accessKey();
    }

    public String secretKey() {
        return credentials.secretKey();
    }

    public record Credentials(
            String accessKey,
            String secretKey
    ) {
    }

    public record Region(
            @Name("static")
            String staticRegion
    ) {
    }

    public record S3(
            String bucket,
            String publicBaseUrl,
            long presignedUrlExpirationMinutes
    ) {
    }

    public record Stack(
            boolean auto
    ) {
    }
}
