package com.sdan.middleware.BuildingBlocks.Application;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
