package com.sdan.middleware.Domain.Exceptions.Node;

import com.sdan.middleware.BuildingBlocks.Application.ResourseNotAvailableException;

public class NoAvailableNodesException extends ResourseNotAvailableException {
    public NoAvailableNodesException(String message) {
        super(message);
    }
}
