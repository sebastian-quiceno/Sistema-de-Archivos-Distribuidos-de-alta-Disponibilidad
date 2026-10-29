package com.sdan.middleware.Domain.Exceptions.Folder;

import com.sdan.middleware.BuildingBlocks.Application.ForbiddenException;

public class FolderDoesNotBelongToUserException extends ForbiddenException {
    public FolderDoesNotBelongToUserException(String message) {
        super(message);
    }
}
