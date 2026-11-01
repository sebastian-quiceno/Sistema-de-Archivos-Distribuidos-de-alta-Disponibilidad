package com.sdan.middleware.Domain.Exceptions.File;

import com.sdan.middleware.BuildingBlocks.Application.ResourceNotFoundException;

public class FileNotFoundException extends ResourceNotFoundException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
