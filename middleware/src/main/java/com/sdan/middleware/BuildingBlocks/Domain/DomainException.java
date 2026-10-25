package com.sdan.middleware.BuildingBlocks.Domain;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
