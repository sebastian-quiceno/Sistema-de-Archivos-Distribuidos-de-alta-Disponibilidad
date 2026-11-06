package com.sdan.middleware.Application.Outputs;


import java.util.UUID;

public record UserCredentialsOutput(
        UUID userId,
        String userName,
        String token
) {}
