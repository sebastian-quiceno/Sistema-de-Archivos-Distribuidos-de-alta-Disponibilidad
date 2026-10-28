package com.sdan.middleware.Infraestructure.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
public class S3Config {

    @Value("${app.aws.region}")
    private String region;
    @Value("${app.aws.access-key-id}")
    private String accessKey;
    @Value("${app.aws.secret-access-key}")
    private String secretKey;
    @Value("${app.aws.session-token}")
    private String sessionToken;

    @Bean
    public S3Presigner s3Presigner() {

        AwsSessionCredentials credentials =
                AwsSessionCredentials.create(
                        accessKey,
                        secretKey,
                        sessionToken
                );

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }
}
