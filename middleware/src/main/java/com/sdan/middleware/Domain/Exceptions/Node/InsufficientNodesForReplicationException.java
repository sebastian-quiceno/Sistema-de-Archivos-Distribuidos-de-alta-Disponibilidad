package com.sdan.middleware.Domain.Exceptions.Node;

import com.sdan.middleware.BuildingBlocks.Application.ProcessingException;

public class InsufficientNodesForReplicationException extends ProcessingException {
    public InsufficientNodesForReplicationException(String message) {
        super(message);
    }
}
