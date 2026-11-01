package com.sdan.middleware.Domain.Exceptions.File;

import com.sdan.middleware.BuildingBlocks.Application.ForbiddenException;

public class FileDoesNotBelongToUserException extends ForbiddenException {
    public FileDoesNotBelongToUserException(String message) {
        super(message);
    }
}
