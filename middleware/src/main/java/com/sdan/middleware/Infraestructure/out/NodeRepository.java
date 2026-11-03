package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Infraestructure.out.Entities.NodeEntity;
import com.sdan.middleware.Infraestructure.out.Entities.StatusEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.NodeMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataNodeRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NodeRepository implements INodeRepository {

    private final SpringDataNodeRepository springDataNodeRepository;
    private final SpringDataStatusRepository springDataStatusRepository;
    private final NodeMapper nodeMapper;

    public NodeRepository(
            SpringDataNodeRepository springDataNodeRepository,
            SpringDataStatusRepository springDataStatusRepository,
            NodeMapper nodeMapper) {
        this.springDataNodeRepository = springDataNodeRepository;
        this.springDataStatusRepository = springDataStatusRepository;
        this.nodeMapper = nodeMapper;
    }

    @Override
    public Optional<Node> getById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataNodeRepository.findById(id).map(nodeMapper::toDomain);
    }

    @Override
    public List<UUID> getIdsOnline() {
        return springDataNodeRepository.findOnlineNodeIds();
    }

    @Override
    public void save(Node node) {
        if (node == null) {
            return;
        }

        String statusName = node.getStatus() != null ? node.getStatus().name() : "ONLINE";
        StatusEntity statusEntity = springDataStatusRepository.findByNameIgnoreCase(statusName)
                .orElseGet(() -> springDataStatusRepository.save(new StatusEntity(UUID.randomUUID(), statusName, "Status " + statusName)));

        NodeEntity entity = nodeMapper.toEntity(node, statusEntity);
        springDataNodeRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        springDataNodeRepository.deleteById(id);
    }
}

