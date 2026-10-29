package com.sdan.middleware.Domain.Exceptions.Node;

import com.sdan.middleware.BuildingBlocks.Application.ResourceNotFoundException;

public class NodeNotFoundException extends ResourceNotFoundException {
    public NodeNotFoundException(String message) {
        super(message);
    }
}
