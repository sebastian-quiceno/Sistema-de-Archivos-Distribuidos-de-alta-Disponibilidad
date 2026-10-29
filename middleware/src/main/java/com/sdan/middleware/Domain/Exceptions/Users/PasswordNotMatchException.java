package com.sdan.middleware.Domain.Exceptions.Users;

public class PasswordNotMatchException extends MismatchException {
    public PasswordNotMatchException(String message) {
        super(message);
    }
}
