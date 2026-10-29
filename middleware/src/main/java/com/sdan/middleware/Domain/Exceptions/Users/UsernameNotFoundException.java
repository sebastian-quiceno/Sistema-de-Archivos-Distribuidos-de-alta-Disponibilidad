package com.sdan.middleware.Domain.Exceptions.Users;

import com.sdan.middleware.BuildingBlocks.Application.ResourceNotFoundException;


public class UsernameNotFoundException extends ResourceNotFoundException {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
