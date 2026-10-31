package com.sdan.middleware.Domain.Exceptions.File;

import com.sdan.middleware.BuildingBlocks.Application.ProcessingException;

public class FileProcessingException extends ProcessingException {
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
