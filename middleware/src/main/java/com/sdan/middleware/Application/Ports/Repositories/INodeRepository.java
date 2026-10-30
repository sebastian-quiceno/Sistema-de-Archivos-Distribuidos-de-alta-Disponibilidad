package com.sdan.middleware.Application.Ports.Repositories;

import com.sdan.middleware.Domain.Entities.Node;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface INodeRepository {
    Optional<Node> getById(UUID id);

    List<UUID> getIdsOnline();

    void save(Node node);

    void deleteById(UUID id);
}
