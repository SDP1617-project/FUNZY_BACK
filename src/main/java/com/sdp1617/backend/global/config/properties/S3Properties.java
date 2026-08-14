package com.sdp1617.backend.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String bucket,
        String region,
        String publicBaseUrl,
        long presignedUrlExpirationMinutes
) {
}
