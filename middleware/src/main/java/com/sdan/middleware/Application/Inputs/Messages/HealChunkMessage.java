package com.sdan.middleware.Application.Inputs.Messages;

import java.util.UUID;

public record HealChunkMessage(
        UUID corruptedChunkId,
        UUID healthyChunkId
) {}

