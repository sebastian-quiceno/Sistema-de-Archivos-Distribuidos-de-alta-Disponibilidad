package com.sdan.middleware.Domain.Exceptions.Chunk;

import com.sdan.middleware.BuildingBlocks.Application.ProcessingException;

public class ChunkCorruptedException extends ProcessingException {
    public ChunkCorruptedException(String message) {
        super(message);
    }
}
