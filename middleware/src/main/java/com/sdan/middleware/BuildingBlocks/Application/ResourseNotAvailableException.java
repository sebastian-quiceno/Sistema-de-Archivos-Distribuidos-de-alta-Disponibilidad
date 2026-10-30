package com.sdan.middleware.BuildingBlocks.Application;

public class ResourseNotAvailableException extends RuntimeException {
    public ResourseNotAvailableException(String message) {
        super(message);
    }
}
