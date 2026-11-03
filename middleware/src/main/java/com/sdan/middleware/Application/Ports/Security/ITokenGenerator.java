package com.sdan.middleware.Application.Ports.Security;

import com.sdan.middleware.Domain.Entities.User;

import java.util.List;
import java.util.UUID;

public interface ITokenGenerator {
    String Generate(User user);

    default String generate(User user) {
        return Generate(user);
    }

    boolean validateToken(String token);

    UUID extractUserId(String token);

    String extractUsername(String token);

    List<String> extractPermissions(String token);
}
