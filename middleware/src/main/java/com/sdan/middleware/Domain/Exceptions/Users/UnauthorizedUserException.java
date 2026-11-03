package com.sdan.middleware.Domain.Exceptions.Users;

import com.sdan.middleware.BuildingBlocks.Domain.DomainException;

public class UnauthorizedUserException extends DomainException {
    public UnauthorizedUserException(String message) {
        super(message);
    }
}

