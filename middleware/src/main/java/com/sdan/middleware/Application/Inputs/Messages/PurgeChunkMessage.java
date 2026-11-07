package com.sdan.middleware.Application.Inputs.Messages;

public record PurgeChunkMessage(
        String nodeName,
        String regionName,
        String fileKey
) {}

