package com.sdan.middleware.Application.Ports.Security;

public interface IPasswordHasher {
    String Hash(String password);
    Boolean Verify(String password, String passwordHash);
}
