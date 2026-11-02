package com.sdan.middleware.BuildingBlocks.Application;

public class InvalidResourseException extends RuntimeException {
    public InvalidResourseException(String message) {
        super(message);
    }
}
