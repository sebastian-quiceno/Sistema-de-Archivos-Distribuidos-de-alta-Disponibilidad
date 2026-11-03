package com.sdan.middleware.Application.Ports.Security;

import java.util.UUID;

public interface IAuthorizationService {
    void authorize(String permission);

    boolean hasPermission(String permission);

    void authorizeOwner(UUID resourceOwnerId);
}
