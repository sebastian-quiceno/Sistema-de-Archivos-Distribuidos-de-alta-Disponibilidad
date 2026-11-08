package com.sdan.middleware.Infraestructure.out.Storage;

/**
 * Excepción lanzada cuando la configuración del servicio de almacenamiento S3
 * es incompleta, inválida o faltan parámetros obligatorios.
 */
public class StorageConfigurationException extends IllegalStateException {

    public StorageConfigurationException(String message) {
        super(message);
    }

    public StorageConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

