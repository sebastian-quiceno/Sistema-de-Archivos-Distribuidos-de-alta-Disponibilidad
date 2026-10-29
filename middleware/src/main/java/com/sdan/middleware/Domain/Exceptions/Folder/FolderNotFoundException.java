package com.sdan.middleware.Domain.Exceptions.Folder;

import com.sdan.middleware.BuildingBlocks.Application.ResourceNotFoundException;

public class FolderNotFoundException extends ResourceNotFoundException {
    public FolderNotFoundException(String message) {
        super(message);
    }
}
