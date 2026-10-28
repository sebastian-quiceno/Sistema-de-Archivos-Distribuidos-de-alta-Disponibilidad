package com.sdan.middleware.Infraestructure.In.DTOs.Response;

public record PresignedUrlResponse(
        String key,
        String url,
        long expiresInSeconds
){}
