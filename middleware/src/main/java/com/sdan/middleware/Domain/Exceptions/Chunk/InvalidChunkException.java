package com.sdan.middleware.Domain.Exceptions.Chunk;

import com.sdan.middleware.BuildingBlocks.Application.InvalidResourseException;

public class InvalidChunkException extends InvalidResourseException {
    public InvalidChunkException(String message) {
        super(message);
    }
}
