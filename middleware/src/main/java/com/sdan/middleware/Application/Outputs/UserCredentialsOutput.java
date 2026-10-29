package com.sdan.middleware.Application.Outputs;

import com.sdan.middleware.Domain.ValueObjects.UserName;

import java.util.UUID;

public record UserCredentialsOutput(
        UUID userId,
        UserName userName,
        String token
) {}
