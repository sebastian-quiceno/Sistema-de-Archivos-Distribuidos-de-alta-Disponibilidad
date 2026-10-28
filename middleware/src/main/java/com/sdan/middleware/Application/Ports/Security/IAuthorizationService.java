package com.sdan.middleware.Application.Ports.Security;

public interface IAuthorizationService {
    void authorize(String permission);
}
