package com.sdan.middleware.Application.Ports.Security;

public interface IPasswordHasher {
    String Hash(String password);

    Boolean Verify(String password, String passwordHash);

    default String hash(String password) {
        return Hash(password);
    }

    default Boolean verify(String password, String passwordHash) {
        return Verify(password, passwordHash);
    }
}
