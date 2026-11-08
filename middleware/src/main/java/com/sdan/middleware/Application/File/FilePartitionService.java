package com.sdan.middleware.Application.File;

import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class FilePartitionService {
    public static final int DEFAULT_MAX_SIZE_PER_CHUNK = 10 * 1024 * 1024;
    public static final int MAX_SIZE_PER_CHUNK = DEFAULT_MAX_SIZE_PER_CHUNK;

    private final int maxChunkSize;

    public FilePartitionService() {
        this(DEFAULT_MAX_SIZE_PER_CHUNK);
    }

    public FilePartitionService(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize > 0 ? maxChunkSize : DEFAULT_MAX_SIZE_PER_CHUNK;
    }

    public int calculateTotalPartitions(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("El tamaño del archivo no puede ser negativo");
        }

        return (int) Math.ceilDiv(size, maxChunkSize);
    }

    public void partition(InputStream input, long declaredSize, Consumer<byte[]> chunkConsumer) {
        long totalBytesRead = 0;

        try (input) {
            byte[] chunkBytes;

            while ((chunkBytes = input.readNBytes(maxChunkSize)).length > 0) {

                totalBytesRead += chunkBytes.length;

                // Evita almacenar un archivo distinto al declarado.
                if (totalBytesRead > declaredSize) {
                    throw new FileProcessingException("El archivo contiene más bytes de los declarados");
                }

                chunkConsumer.accept(chunkBytes);
            }

            if (totalBytesRead != declaredSize) {
                throw new FileProcessingException(
                        "El tamaño real del archivo no coincide con el tamaño declarado. Esperado: " + declaredSize + ", leído: " + totalBytesRead
                );
            }

        } catch (IOException e) {
            throw new FileProcessingException("Error al leer o procesar el archivo\n", e);
        }
    }

}

