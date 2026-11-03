package com.sdan.middleware.Infraestructure.out.Mappers;

import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Enums.Status;
import com.sdan.middleware.Domain.ValueObjects.NodeName;
import com.sdan.middleware.Domain.ValueObjects.RegionName;
import com.sdan.middleware.Infraestructure.out.Entities.NodeEntity;
import com.sdan.middleware.Infraestructure.out.Entities.StatusEntity;
import org.springframework.stereotype.Component;

@Component
public class NodeMapper {

    public Node toDomain(NodeEntity entity) {
        if (entity == null) {
            return null;
        }

        Status status = Status.ONLINE;
        if (entity.getStatus() != null && entity.getStatus().getName() != null) {
            try {
                status = Status.valueOf(entity.getStatus().getName().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Node.reCreate(
                entity.getNodeId(),
                new NodeName(entity.getName()),
                new RegionName(entity.getRegionName()),
                status,
                entity.getMaxDiskSize(),
                entity.getFreeDiskSize(),
                entity.getLastHeatbeat()
        );
    }

    public NodeEntity toEntity(Node domain, StatusEntity statusEntity) {
        if (domain == null) {
            return null;
        }
        return new NodeEntity(
                domain.getId(),
                statusEntity,
                domain.getName() != null ? domain.getName().getValue() : null,
                domain.getRegionName() != null ? domain.getRegionName().getValue() : null,
                domain.getMaxDiskSize(),
                domain.getFreeDiskSize(),
                domain.getLastHeatbeat()
        );
    }
}

