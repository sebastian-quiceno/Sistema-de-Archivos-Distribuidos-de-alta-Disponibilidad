package com.sdan.middleware.Domain.Exceptions.FileType;

import com.sdan.middleware.BuildingBlocks.Application.ResourceNotFoundException;

public class FileTypeNotFoundException extends ResourceNotFoundException {
    public FileTypeNotFoundException(String message) {
        super(message);
    }
}
