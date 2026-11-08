package com.sdan.middleware.Infraestructure.out.Storage;

import com.sdan.middleware.BuildingBlocks.Application.ProcessingException;

/**
 * Excepción lanzada cuando ocurre un error durante una operación
 * de almacenamiento en Amazon S3 (carga, descarga, copia o eliminación).
 */
public class StorageOperationException extends ProcessingException {

    public StorageOperationException(String message) {
        super(message);
    }

    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

