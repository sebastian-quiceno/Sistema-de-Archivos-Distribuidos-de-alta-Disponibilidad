package com.sdan.middleware.Domain.Exceptions.Users;

import com.sdan.middleware.BuildingBlocks.Application.ResourceAlreadyExistException;


public class UsernameAlreadyExist extends ResourceAlreadyExistException {
    public UsernameAlreadyExist(String message) {
        super(message);
    }
}
