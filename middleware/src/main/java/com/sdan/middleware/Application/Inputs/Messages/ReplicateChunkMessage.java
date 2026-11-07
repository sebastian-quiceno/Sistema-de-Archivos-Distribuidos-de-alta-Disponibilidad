package com.sdan.middleware.Application.Inputs.Messages;

import java.util.UUID;

public record ReplicateChunkMessage(
        UUID chunkId,
        int replicationFactor
) {}

