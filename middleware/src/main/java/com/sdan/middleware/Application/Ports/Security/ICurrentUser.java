package com.sdan.middleware.Application.Ports.Security;

import java.util.UUID;

public interface ICurrentUser {
    UUID getUserId();

    String getUsername();

    boolean isAuthenticated();
}
