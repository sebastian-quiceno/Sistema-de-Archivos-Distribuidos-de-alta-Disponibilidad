package com.sdan.middleware.Infraestructure.out.Storage;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    private S3StorageService storageService;

    private final String defaultBucket = "test-dfs-bucket";
    private final String defaultRegion = "us-east-1";

    @BeforeEach
    void setUp() {
        Map<String, String> nodeBuckets = Map.of(
                "node-1", "bucket-node-1",
                "node-2", "bucket-node-2"
        );
        storageService = new S3StorageService(s3Client, null, defaultRegion, defaultBucket, null, nodeBuckets);
    }

    // =========================================================================
    // 1. Pruebas de uploadObject
    // =========================================================================

    @Test
    @DisplayName("uploadObject con datos válidos invoca putObject en S3 con los metadatos correctos")
    void uploadObject_withValidData_callsPutObjectSuccessfully() {
        byte[] data = "contenido de prueba para upload".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        String key = "chunks/chunk-001.bin";
        String contentType = "application/pdf";

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        storageService.uploadObject("node-1", defaultRegion, key, stream, data.length, contentType);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals("bucket-node-1", capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
        assertEquals((long) data.length, capturedRequest.contentLength());
        assertEquals(contentType, capturedRequest.contentType());
    }

    @Test
    @DisplayName("uploadObject con contentType nulo usa application/octet-stream por defecto")
    void uploadObject_withNullContentType_defaultsToOctetStream() {
        byte[] data = "datos".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        storageService.uploadObject("node-1", defaultRegion, "key-123", stream, data.length, null);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("application/octet-stream", captor.getValue().contentType());
    }

    @Test
    @DisplayName("uploadObject con clave nula o vacía lanza IllegalArgumentException")
    void uploadObject_withNullOrBlankKey_throwsIllegalArgumentException() {
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        assertThrows(IllegalArgumentException.class,
                () -> storageService.uploadObject("node-1", defaultRegion, null, stream, 3, "text/plain"));
        assertThrows(IllegalArgumentException.class,
                () -> storageService.uploadObject("node-1", defaultRegion, "   ", stream, 3, "text/plain"));
    }

    @Test
    @DisplayName("uploadObject con InputStream nulo lanza IllegalArgumentException")
    void uploadObject_withNullStream_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> storageService.uploadObject("node-1", defaultRegion, "key", null, 10, "text/plain"));
    }

    @Test
    @DisplayName("uploadObject con contentLength negativo lanza IllegalArgumentException")
    void uploadObject_withNegativeContentLength_throwsIllegalArgumentException() {
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[]{1});
        assertThrows(IllegalArgumentException.class,
                () -> storageService.uploadObject("node-1", defaultRegion, "key", stream, -1, "text/plain"));
    }

    @Test
    @DisplayName("uploadObject cuando S3 lanza AwsServiceException 403 lo traduce a StorageOperationException")
    void uploadObject_whenS3ThrowsForbidden_translatesException() {
        byte[] data = "datos".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .statusCode(403)
                .message("Access Denied")
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Access Denied").errorCode("AccessDenied").build())
                .build();

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(s3Exception);

        StorageOperationException thrown = assertThrows(StorageOperationException.class,
                () -> storageService.uploadObject("node-1", defaultRegion, "key-fail", stream, data.length, "text/plain"));

        assertNotNull(thrown.getMessage());
    }

    @Test
    @DisplayName("uploadObject cuando falla la red lanza StorageOperationException")
    void uploadObject_whenNetworkFails_translatesException() {
        byte[] data = "datos".getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("Connection timeout"));

        assertThrows(StorageOperationException.class,
                () -> storageService.uploadObject("node-1", defaultRegion, "key-fail", stream, data.length, "text/plain"));
    }

    // =========================================================================
    // 2. Pruebas de getObject
    // =========================================================================

    @Test
    @DisplayName("getObject con clave existente retorna el ResponseInputStream de S3")
    void getObject_withExistingKey_returnsStream() throws Exception {
        String key = "chunks/chunk-001.bin";
        byte[] expectedBytes = "contenido descargado".getBytes(StandardCharsets.UTF_8);

        ResponseInputStream<GetObjectResponse> mockResponseStream =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().contentLength((long) expectedBytes.length).build(),
                        new ByteArrayInputStream(expectedBytes)
                );

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseStream);

        InputStream resultStream = storageService.getObject("node-1", defaultRegion, key);

        assertNotNull(resultStream);
        assertArrayEquals(expectedBytes, resultStream.readAllBytes());

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertEquals("bucket-node-1", captor.getValue().bucket());
        assertEquals(key, captor.getValue().key());
    }

    @Test
    @DisplayName("getObject cuando la clave no existe (NoSuchKeyException) retorna null de forma segura")
    void getObject_whenNoSuchKey_returnsNull() {
        String key = "inexistente.bin";

        NoSuchKeyException exception = (NoSuchKeyException) NoSuchKeyException.builder()
                .statusCode(404)
                .message("The specified key does not exist.")
                .build();

        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(exception);

        InputStream result = storageService.getObject("node-1", defaultRegion, key);
        assertNull(result);
    }

    @Test
    @DisplayName("getObject cuando S3 retorna 404 Not Found retorna null")
    void getObject_whenS3Throws404_returnsNull() {
        S3Exception exception = (S3Exception) S3Exception.builder()
                .statusCode(404)
                .message("Not Found")
                .build();

        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(exception);

        InputStream result = storageService.getObject("node-1", defaultRegion, "missing.bin");
        assertNull(result);
    }

    @Test
    @DisplayName("getObject con clave nula o vacía lanza IllegalArgumentException")
    void getObject_withBlankKey_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> storageService.getObject("node-1", defaultRegion, ""));
        assertThrows(IllegalArgumentException.class, () -> storageService.getObject("node-1", defaultRegion, null));
    }

    @Test
    @DisplayName("getObject cuando ocurre un error 403 Forbidden lanza StorageOperationException")
    void getObject_whenForbidden_throwsStorageOperationException() {
        S3Exception exception = (S3Exception) S3Exception.builder()
                .statusCode(403)
                .message("Access Denied")
                .build();

        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(exception);

        assertThrows(StorageOperationException.class,
                () -> storageService.getObject("node-1", defaultRegion, "forbidden.bin"));
    }

    // =========================================================================
    // 3. Pruebas de deleteFileByKey
    // =========================================================================

    @Test
    @DisplayName("deleteFileByKey con clave válida ejecuta deleteObject en S3")
    void deleteFileByKey_withValidData_callsDeleteObject() {
        String key = "chunks/chunk-delete.bin";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        storageService.deleteFileByKey("node-1", defaultRegion, key);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("bucket-node-1", captor.getValue().bucket());
        assertEquals(key, captor.getValue().key());
    }

    @Test
    @DisplayName("deleteFileByKey con clave vacía o nula no ejecuta ninguna llamada a S3")
    void deleteFileByKey_withBlankKey_doesNothing() {
        storageService.deleteFileByKey("node-1", defaultRegion, null);
        storageService.deleteFileByKey("node-1", defaultRegion, "   ");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteFileByKey cuando la clave no existe en S3 es idempotente y no lanza excepción")
    void deleteFileByKey_whenKeyNotFound_isIdempotent() {
        NoSuchKeyException exception = (NoSuchKeyException) NoSuchKeyException.builder()
                .statusCode(404)
                .message("The specified key does not exist.")
                .build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(exception);

        // No debe propagar la excepción
        storageService.deleteFileByKey("node-1", defaultRegion, "already-deleted.bin");
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteFileByKey cuando falla por error de servicio lanza StorageOperationException")
    void deleteFileByKey_whenS3Fails_throwsStorageOperationException() {
        S3Exception exception = (S3Exception) S3Exception.builder()
                .statusCode(500)
                .message("Internal S3 Error")
                .build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(exception);

        assertThrows(StorageOperationException.class,
                () -> storageService.deleteFileByKey("node-1", defaultRegion, "key-500.bin"));
    }

    // =========================================================================
    // 4. Pruebas de copyToAnotherNode
    // =========================================================================

    @Test
    @DisplayName("copyToAnotherNode con datos válidos ejecuta copyObject nativo de S3")
    void copyToAnotherNode_withValidData_executesNativeCopy() {
        CopyToAnotherNodeCommand command = new CopyToAnotherNodeCommand(
                defaultRegion,
                "node-1",
                "chunk-copy.bin",
                defaultRegion,
                "node-2"
        );

        when(s3Client.copyObject(any(CopyObjectRequest.class)))
                .thenReturn(CopyObjectResponse.builder().build());

        storageService.copyToAnotherNode(command);

        ArgumentCaptor<CopyObjectRequest> captor = ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(captor.capture());

        CopyObjectRequest req = captor.getValue();
        assertEquals("bucket-node-1/chunk-copy.bin", req.copySource());
        assertEquals("bucket-node-2", req.bucket());
        assertEquals("chunk-copy.bin", req.key());
    }

    @Test
    @DisplayName("copyToAnotherNode con origen y destino idénticos no ejecuta operaciones")
    void copyToAnotherNode_sameSourceAndDestination_doesNothing() {
        CopyToAnotherNodeCommand command = new CopyToAnotherNodeCommand(
                defaultRegion,
                "node-1",
                "chunk-same.bin",
                defaultRegion,
                "node-1"
        );

        storageService.copyToAnotherNode(command);

        verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("copyToAnotherNode cuando copyObject nativo falla ejecuta copia por streaming")
    void copyToAnotherNode_whenNativeCopyFails_fallsBackToStreamCopy() {
        CopyToAnotherNodeCommand command = new CopyToAnotherNodeCommand(
                defaultRegion,
                "node-1",
                "chunk-fallback.bin",
                defaultRegion,
                "node-2"
        );

        // Falla la copia nativa
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(403).message("Cross-region copy denied").build());

        // El streaming lee el origen exitosamente
        byte[] sourceBytes = "datos replicados por stream".getBytes(StandardCharsets.UTF_8);
        ResponseInputStream<GetObjectResponse> mockStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) sourceBytes.length).build(),
                new ByteArrayInputStream(sourceBytes)
        );
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

        // La subida al destino es exitosa
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        storageService.copyToAnotherNode(command);

        verify(s3Client, times(1)).copyObject(any(CopyObjectRequest.class));
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("copyToAnotherNode con comando nulo o clave vacía lanza IllegalArgumentException")
    void copyToAnotherNode_withNullOrBlank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> storageService.copyToAnotherNode(null));

        CopyToAnotherNodeCommand emptyKeyCommand = new CopyToAnotherNodeCommand(
                defaultRegion, "node-1", "   ", defaultRegion, "node-2"
        );
        assertThrows(IllegalArgumentException.class, () -> storageService.copyToAnotherNode(emptyKeyCommand));
    }

    // =========================================================================
    // 5. Pruebas de resolución de buckets y regiones
    // =========================================================================

    @Test
    @DisplayName("resolveBucket utiliza el mapa de nodos configurado")
    void resolveBucket_usesConfiguredNodeMap() {
        assertEquals("bucket-node-1", storageService.resolveBucket("node-1"));
        assertEquals("bucket-node-2", storageService.resolveBucket("node-2"));
    }

    @Test
    @DisplayName("resolveBucket utiliza el nombre del nodo directamente o el bucket por defecto si es nulo")
    void resolveBucket_fallsBackToDefaultBucket() {
        assertEquals("node-999", storageService.resolveBucket("node-999"));
        assertEquals(defaultBucket, storageService.resolveBucket(null));
        assertEquals(defaultBucket, storageService.resolveBucket(""));
    }

    @Test
    @DisplayName("resolveBucket si no hay bucket por defecto ni mapeo lanza StorageOperationException")
    void resolveBucket_whenNoBucketAvailable_throwsStorageOperationException() {
        S3StorageService unconfiguredService = new S3StorageService(s3Client, null, defaultRegion, "", null, Map.of());

        assertThrows(StorageOperationException.class, () -> unconfiguredService.resolveBucket(null));
    }

    @Test
    @DisplayName("resolveRegion retorna la región suministrada o el valor por defecto si es nula")
    void resolveRegion_returnsSuppliedOrDefault() {
        assertEquals("us-west-2", storageService.resolveRegion("us-west-2"));
        assertEquals(defaultRegion, storageService.resolveRegion(null));
        assertEquals(defaultRegion, storageService.resolveRegion("   "));
    }
}

