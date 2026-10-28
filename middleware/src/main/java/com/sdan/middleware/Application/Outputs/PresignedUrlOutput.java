package com.sdan.middleware.Application.Outputs;

public record PresignedUrlOutput(
        String key,
        String url,
        long expiresInSeconds
){}
