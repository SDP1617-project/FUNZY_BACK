package com.sdp1617.backend.global.config;

import com.sdp1617.backend.global.config.properties.S3Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.staticRegion()));
        if (properties.hasStaticCredentials()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
            ));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.staticRegion()));
        if (properties.hasStaticCredentials()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
            ));
        }
        return builder.build();
    }
}
