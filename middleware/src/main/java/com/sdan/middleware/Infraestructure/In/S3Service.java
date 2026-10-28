package com.sdan.middleware.Infraestructure.In;

import com.sdan.middleware.Domain.ValueObjects.FileName;
import com.sdan.middleware.Infraestructure.In.DTOs.Response.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${app.aws.s3.bucket}")
    private String bucketName;

    private final S3Presigner presigner;

    public S3Service(S3Presigner presigner) {
        this.presigner = presigner;
    }

    public PresignedUrlResponse generateUploadUrl(FileName filename) {

        String key = UUID.randomUUID() + "-" + filename.getValue();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(objectRequest)
                        .build();

        String url = presigner.presignPutObject(presignRequest)
                .url()
                .toString();

        return new PresignedUrlResponse(key, url, 300);
    }

    public PresignedUrlResponse generateDownloadUrl(String key) {

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(objectRequest)
                        .build();

        String url = presigner.presignGetObject(presignRequest)
                .url()
                .toString();

        return new PresignedUrlResponse(key, url, 300);
    }

}
