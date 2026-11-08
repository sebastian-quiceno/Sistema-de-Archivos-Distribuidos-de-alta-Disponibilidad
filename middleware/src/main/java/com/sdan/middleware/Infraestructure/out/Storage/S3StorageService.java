package com.sdan.middleware.Infraestructure.out.Storage;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementación concreta de {@link IStorageService} que interactúa con Amazon S3
 * mediante el AWS SDK para Java 2.x.
 *
 * <p>Esta clase administra operaciones de almacenamiento de objetos (carga, descarga,
 * copia entre nodos y eliminación) resolviendo dinámicamente buckets y regiones por nodo,
 * manteniendo un pool de clientes de S3 reutilizables por región y garantizando el
 * manejo adecuado de recursos y excepciones.</p>
 */
@Service
public class S3StorageService implements IStorageService, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client defaultS3Client;
    private final AwsCredentialsProvider credentialsProvider;
    private final String defaultRegion;
    private final String defaultBucket;
    private final String endpointOverride;
    private final Map<String, String> nodeBuckets;
    private final ConcurrentMap<String, S3Client> regionClients = new ConcurrentHashMap<>();

    @Autowired
    public S3StorageService(
            S3Client defaultS3Client,
            AwsCredentialsProvider credentialsProvider,
            @Value("${app.aws.region:us-east-1}") String defaultRegion,
            @Value("${app.aws.s3.bucket:test-bucket}") String defaultBucket,
            @Value("${app.aws.s3.endpoint:}") String endpointOverride) {
        this(defaultS3Client, credentialsProvider, defaultRegion, defaultBucket, endpointOverride, Collections.emptyMap());
    }

    public S3StorageService(
            S3Client defaultS3Client,
            AwsCredentialsProvider credentialsProvider,
            String defaultRegion,
            String defaultBucket,
            String endpointOverride,
            Map<String, String> nodeBuckets) {
        this.defaultS3Client = defaultS3Client;
        this.credentialsProvider = credentialsProvider;
        this.defaultRegion = (defaultRegion != null && !defaultRegion.isBlank()) ? defaultRegion.trim() : "us-east-1";
        this.defaultBucket = (defaultBucket != null && !defaultBucket.isBlank()) ? defaultBucket.trim() : "";
        this.endpointOverride = (endpointOverride != null && !endpointOverride.isBlank()) ? endpointOverride.trim() : null;
        this.nodeBuckets = nodeBuckets != null ? new ConcurrentHashMap<>(nodeBuckets) : new ConcurrentHashMap<>();

        if (defaultS3Client != null) {
            this.regionClients.put(this.defaultRegion.toLowerCase(), defaultS3Client);
        }
    }

    /**
     * Constructor auxiliar para pruebas unitarias.
     */
    public S3StorageService(S3Client defaultS3Client, String defaultBucket) {
        this(defaultS3Client, null, "us-east-1", defaultBucket, null, Collections.emptyMap());
    }

    /**
     * Constructor auxiliar para pruebas unitarias con mapeo explícito de buckets por nodo.
     */
    public S3StorageService(S3Client defaultS3Client, String defaultBucket, Map<String, String> nodeBuckets) {
        this(defaultS3Client, null, "us-east-1", defaultBucket, null, nodeBuckets);
    }

    @Override
    public void uploadObject(String nodeName, String region, String key, InputStream content, long contentLength, String contentType) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave (key) del objeto no puede ser nula o vacía");
        }
        if (content == null) {
            throw new IllegalArgumentException("El contenido (InputStream) no puede ser nulo");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("El contentLength no puede ser negativo: " + contentLength);
        }

        String bucket = resolveBucket(nodeName);
        String effectiveRegion = resolveRegion(region);
        S3Client client = getClientForRegion(effectiveRegion);

        String effectiveContentType = (contentType != null && !contentType.isBlank())
                ? contentType
                : "application/octet-stream";

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentLength(contentLength)
                    .contentType(effectiveContentType)
                    .build();

            client.putObject(putRequest, RequestBody.fromInputStream(content, contentLength));
            log.debug("Objeto '{}' cargado exitosamente en el bucket '{}' (región: '{}')", key, bucket, effectiveRegion);
        } catch (AwsServiceException e) {
            throw translateAwsException("Error al subir el objeto '" + key + "' al bucket '" + bucket + "'", e);
        } catch (SdkClientException e) {
            throw new StorageOperationException("Error de comunicación con AWS S3 al subir el objeto '" + key + "': " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getObject(String nodeName, String region, String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave (key) del objeto no puede ser nula o vacía");
        }

        String bucket = resolveBucket(nodeName);
        String effectiveRegion = resolveRegion(region);
        S3Client client = getClientForRegion(effectiveRegion);

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> responseStream = client.getObject(getRequest);
            log.debug("Flujo obtenido para el objeto '{}' desde el bucket '{}' (región: '{}')", key, bucket, effectiveRegion);
            return responseStream;
        } catch (NoSuchKeyException e) {
            log.warn("El objeto '{}' no existe en el bucket '{}' (NoSuchKey)", key, bucket);
            return null;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("El objeto '{}' no fue encontrado en el bucket '{}' (404 Not Found)", key, bucket);
                return null;
            }
            throw translateAwsException("Error al obtener el objeto '" + key + "' del bucket '" + bucket + "'", e);
        } catch (SdkClientException e) {
            throw new StorageOperationException("Error de comunicación con AWS S3 al obtener el objeto '" + key + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFileByKey(String nodeName, String region, String key) {
        if (key == null || key.isBlank()) {
            log.debug("Clave nula o vacía al intentar eliminar; operación ignorada.");
            return;
        }

        String bucket = resolveBucket(nodeName);
        String effectiveRegion = resolveRegion(region);
        S3Client client = getClientForRegion(effectiveRegion);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            client.deleteObject(deleteRequest);
            log.debug("Objeto '{}' eliminado exitosamente del bucket '{}' (región: '{}')", key, bucket, effectiveRegion);
        } catch (NoSuchKeyException e) {
            // S3 delete es idempotente; si la clave no existe, se considera completada
            log.debug("El objeto '{}' ya no existía en el bucket '{}'", key, bucket);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return;
            }
            throw translateAwsException("Error al eliminar el objeto '" + key + "' del bucket '" + bucket + "'", e);
        } catch (SdkClientException e) {
            throw new StorageOperationException("Error de comunicación con AWS S3 al eliminar el objeto '" + key + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void copyToAnotherNode(CopyToAnotherNodeCommand dto) {
        if (dto == null) {
            throw new IllegalArgumentException("El comando de copia CopyToAnotherNodeCommand no puede ser nulo");
        }
        if (dto.key() == null || dto.key().isBlank()) {
            throw new IllegalArgumentException("La clave (key) en el comando de copia no puede ser nula o vacía");
        }

        String sourceBucket = resolveBucket(dto.bucketName());
        String sourceRegion = resolveRegion(dto.region());
        String destBucket = resolveBucket(dto.arriveBucketName());
        String destRegion = resolveRegion(dto.arriveRegion());
        String key = dto.key();

        if (sourceBucket.equals(destBucket) && sourceRegion.equalsIgnoreCase(destRegion)) {
            log.debug("Origen y destino idénticos para el objeto '{}' en '{}'; no se requiere copia.", key, sourceBucket);
            return;
        }

        S3Client destClient = getClientForRegion(destRegion);

        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .copySource(sourceBucket + "/" + key)
                    .bucket(destBucket)
                    .key(key)
                    .build();

            destClient.copyObject(copyRequest);
            log.debug("Copia nativa de S3 completada: '{}/{}' -> '{}/{}'", sourceBucket, key, destBucket, key);
        } catch (Exception nativeCopyException) {
            log.warn("La copia nativa de S3 falló para el objeto '{}' ({}); ejecutando copia mediante flujo de streaming.",
                    key, nativeCopyException.getMessage());
            copyViaStream(dto.bucketName(), sourceRegion, dto.arriveBucketName(), destRegion, key, nativeCopyException);
        }
    }

    /**
     * Mecanismo de copia alternativo mediante flujo binario, utilizado cuando la operación
     * nativa de S3 (CopyObject) no es compatible debido a políticas cruzadas o diferencias de cuentas/regiones.
     */
    private void copyViaStream(String sourceNode, String sourceRegion, String destNode, String destRegion, String key, Exception originalError) {
        try (InputStream sourceStream = getObject(sourceNode, sourceRegion, key)) {
            if (sourceStream == null) {
                throw new StorageOperationException(
                        "No se encontró el objeto de origen '" + key + "' para realizar la copia", originalError
                );
            }

            byte[] bytes = sourceStream.readAllBytes();
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)) {
                uploadObject(destNode, destRegion, key, byteStream, bytes.length, "application/octet-stream");
            }
            log.debug("Copia mediante streaming completada para el objeto '{}' hacia '{}'", key, destNode);
        } catch (IOException e) {
            throw new StorageOperationException("Error durante la lectura del flujo al copiar el objeto '" + key + "'", e);
        }
    }

    /**
     * Resuelve el nombre del bucket de S3 correspondiente al nodo o identificador recibido.
     */
    public String resolveBucket(String nodeOrBucketName) {
        if (nodeOrBucketName != null && !nodeOrBucketName.isBlank()) {
            String trimmed = nodeOrBucketName.trim();

            // 1. Verificar si está en el mapa explícito de configuración
            if (nodeBuckets.containsKey(trimmed) && !nodeBuckets.get(trimmed).isBlank()) {
                return nodeBuckets.get(trimmed);
            }

            // 2. Verificar variable de entorno por nodo (ej. S3_BUCKET_NODE_1)
            String envVarName = "S3_BUCKET_" + trimmed.replace("-", "_").toUpperCase();
            String envValue = System.getenv(envVarName);
            if (envValue != null && !envValue.isBlank()) {
                return envValue.trim();
            }

            // 3. Si el nombre coincide con el bucket por defecto
            if (trimmed.equalsIgnoreCase(defaultBucket)) {
                return defaultBucket;
            }
        }

        // 4. Fallback al bucket principal configurado
        if (defaultBucket != null && !defaultBucket.isBlank()) {
            return defaultBucket;
        }

        throw new StorageOperationException(
                "No se pudo resolver el bucket S3 para el nodo '" + nodeOrBucketName + "'. " +
                "Configure la variable 'S3_BUCKET_NAME' o defina una variable específica para el nodo."
        );
    }

    /**
     * Resuelve la región de AWS a utilizar.
     */
    public String resolveRegion(String region) {
        if (region != null && !region.isBlank()) {
            return region.trim();
        }
        return defaultRegion;
    }

    /**
     * Retorna o inicializa el cliente {@link S3Client} correspondiente a la región indicada.
     */
    public S3Client getClientForRegion(String region) {
        String effectiveRegion = resolveRegion(region);

        if (effectiveRegion.equalsIgnoreCase(defaultRegion) && defaultS3Client != null) {
            return defaultS3Client;
        }

        return regionClients.computeIfAbsent(effectiveRegion.toLowerCase(), this::buildClientForRegion);
    }

    private S3Client buildClientForRegion(String regionStr) {
        if (credentialsProvider == null) {
            if (defaultS3Client != null) {
                return defaultS3Client;
            }
            throw new StorageOperationException("No se dispone de un AwsCredentialsProvider para crear un cliente S3 para la región: " + regionStr);
        }

        try {
            S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(regionStr))
                    .credentialsProvider(credentialsProvider);

            if (endpointOverride != null && !endpointOverride.isBlank()) {
                builder.endpointOverride(URI.create(endpointOverride))
                        .forcePathStyle(true);
            }

            return builder.build();
        } catch (Exception e) {
            throw new StorageOperationException("No fue posible inicializar el cliente S3 para la región '" + regionStr + "': " + e.getMessage(), e);
        }
    }

    private RuntimeException translateAwsException(String contextMessage, AwsServiceException e) {
        int statusCode = e.statusCode();
        String awsMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
        String fullMessage = contextMessage + " [Status HTTP " + statusCode + ": " + awsMessage + "]";

        if (statusCode == 403) {
            return new StorageOperationException("Acceso denegado en AWS S3: " + fullMessage, e);
        }
        if (statusCode == 404) {
            return new StorageOperationException("Recurso o bucket no encontrado en AWS S3: " + fullMessage, e);
        }
        return new StorageOperationException(fullMessage, e);
    }

    @Override
    @PreDestroy
    public void close() {
        for (Map.Entry<String, S3Client> entry : regionClients.entrySet()) {
            if (entry.getValue() != defaultS3Client) {
                try {
                    entry.getValue().close();
                } catch (Exception e) {
                    log.warn("Error cerrando S3Client para la región '{}': {}", entry.getKey(), e.getMessage());
                }
            }
        }
        regionClients.clear();
    }
}

