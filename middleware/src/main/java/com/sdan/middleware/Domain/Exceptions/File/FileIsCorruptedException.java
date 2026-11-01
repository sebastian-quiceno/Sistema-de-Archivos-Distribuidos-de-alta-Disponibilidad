package com.sdan.middleware.Domain.Exceptions.File;

import com.sdan.middleware.BuildingBlocks.Application.ProcessingException;

public class FileIsCorruptedException extends ProcessingException {
    public FileIsCorruptedException(String message) {
        super(message);
    }
}
