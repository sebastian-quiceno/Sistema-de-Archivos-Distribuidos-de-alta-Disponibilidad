package com.sdan.middleware.Infraestructure.Config;

import com.sdan.middleware.Infraestructure.out.Storage.StorageConfigurationException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    private static final Logger log = LoggerFactory.getLogger(S3Config.class);

    @Value("${app.aws.region:us-east-1}")
    private String region;

    @Value("${app.aws.access-key-id:}")
    private String accessKey;

    @Value("${app.aws.secret-access-key:}")
    private String secretKey;

    @Value("${app.aws.session-token:}")
    private String sessionToken;

    @Value("${app.aws.s3.bucket:}")
    private String defaultBucket;

    @Value("${app.aws.s3.endpoint:}")
    private String endpoint;

    @PostConstruct
    public void validate() {
        if (region == null || region.isBlank()) {
            throw new StorageConfigurationException("La región de AWS no puede estar vacía. Defina la variable de entorno 'AWS_REGION'.");
        }
        if (defaultBucket == null || defaultBucket.isBlank()) {
            log.warn("No se ha configurado 'S3_BUCKET_NAME' (app.aws.s3.bucket). El servicio de almacenamiento requerirá configuración de buckets por nodo.");
        }
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        boolean hasAccessKey = accessKey != null && !accessKey.isBlank() && !accessKey.equals("test-access-key");
        boolean hasSecretKey = secretKey != null && !secretKey.isBlank() && !secretKey.equals("test-secret-key");
        boolean hasSessionToken = sessionToken != null && !sessionToken.isBlank() && !sessionToken.equals("test-session-token");

        if (hasAccessKey && hasSecretKey) {
            if (hasSessionToken) {
                return StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(accessKey, secretKey, sessionToken)
                );
            }
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        }

        // Si no hay credenciales estáticas explícitas válidas, se utiliza el DefaultCredentialsProvider de AWS SDK
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .forcePathStyle(true);
        }

        return builder.build();
    }
}
