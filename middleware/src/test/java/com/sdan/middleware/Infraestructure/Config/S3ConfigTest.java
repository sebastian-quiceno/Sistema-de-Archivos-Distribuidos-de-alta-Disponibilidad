package com.sdan.middleware.Infraestructure.Config;

import com.sdan.middleware.Infraestructure.out.Storage.StorageConfigurationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3ConfigTest {

    @Test
    @DisplayName("validate lanza StorageConfigurationException si region es nula o vacía")
    void validate_whenRegionIsBlank_throwsStorageConfigurationException() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "region", "");
        ReflectionTestUtils.setField(config, "defaultBucket", "some-bucket");

        assertThrows(StorageConfigurationException.class, config::validate);
    }

    @Test
    @DisplayName("awsCredentialsProvider con claves y sessionToken retorna AwsSessionCredentials")
    void awsCredentialsProvider_withSessionToken_returnsSessionCredentials() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "region", "us-east-1");
        ReflectionTestUtils.setField(config, "accessKey", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(config, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(config, "sessionToken", "AQoDYXdzEJr1EXAMPLE");

        AwsCredentialsProvider provider = config.awsCredentialsProvider();
        assertInstanceOf(StaticCredentialsProvider.class, provider);

        AwsCredentials credentials = provider.resolveCredentials();
        assertInstanceOf(AwsSessionCredentials.class, credentials);
        assertEquals("AKIAIOSFODNN7EXAMPLE", credentials.accessKeyId());
        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", credentials.secretAccessKey());
        assertEquals("AQoDYXdzEJr1EXAMPLE", ((AwsSessionCredentials) credentials).sessionToken());
    }

    @Test
    @DisplayName("awsCredentialsProvider con claves sin sessionToken retorna AwsBasicCredentials")
    void awsCredentialsProvider_withoutSessionToken_returnsBasicCredentials() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "region", "us-east-1");
        ReflectionTestUtils.setField(config, "accessKey", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(config, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(config, "sessionToken", "");

        AwsCredentialsProvider provider = config.awsCredentialsProvider();
        assertInstanceOf(StaticCredentialsProvider.class, provider);

        AwsCredentials credentials = provider.resolveCredentials();
        assertEquals("AKIAIOSFODNN7EXAMPLE", credentials.accessKeyId());
        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", credentials.secretAccessKey());
    }

    @Test
    @DisplayName("awsCredentialsProvider sin claves válidas retorna DefaultCredentialsProvider")
    void awsCredentialsProvider_withoutKeys_returnsDefaultProvider() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "region", "us-east-1");
        ReflectionTestUtils.setField(config, "accessKey", "");
        ReflectionTestUtils.setField(config, "secretKey", "");
        ReflectionTestUtils.setField(config, "sessionToken", "");

        AwsCredentialsProvider provider = config.awsCredentialsProvider();
        assertInstanceOf(DefaultCredentialsProvider.class, provider);
    }

    @Test
    @DisplayName("s3Client y s3Presigner se inicializan correctamente")
    void s3ClientAndPresigner_initializeSuccessfully() {
        S3Config config = new S3Config();
        ReflectionTestUtils.setField(config, "region", "us-east-1");
        ReflectionTestUtils.setField(config, "accessKey", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(config, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(config, "sessionToken", "TOKEN123");
        ReflectionTestUtils.setField(config, "defaultBucket", "bucket-test");
        ReflectionTestUtils.setField(config, "endpoint", "");

        AwsCredentialsProvider provider = config.awsCredentialsProvider();
        S3Client client = config.s3Client(provider);
        S3Presigner presigner = config.s3Presigner(provider);

        assertNotNull(client);
        assertNotNull(presigner);

        client.close();
        presigner.close();
    }
}

