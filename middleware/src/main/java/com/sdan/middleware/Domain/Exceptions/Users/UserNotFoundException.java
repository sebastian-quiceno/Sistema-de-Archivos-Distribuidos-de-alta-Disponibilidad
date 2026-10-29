package com.sdan.middleware.Domain.Exceptions.Users;

import com.sdan.middleware.BuildingBlocks.Application.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
