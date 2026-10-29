package com.sdan.middleware.Domain.Exceptions.Users;

public class MismatchException extends RuntimeException {
    public MismatchException(String message) {
        super(message);
    }
}
