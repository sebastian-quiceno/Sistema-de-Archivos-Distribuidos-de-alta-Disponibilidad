package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Domain.Exceptions.Node.InsufficientNodesForReplicationException;
import com.sdan.middleware.Domain.Exceptions.Node.NoAvailableNodesException;

import java.util.*;

public class NodeAssignmentService {
    public static final int REPLICATION_FACTOR = 3;
    public static final int REPLICAITION_FACTOR = REPLICATION_FACTOR;

    private final INodeRepository nodeRepository;

    public NodeAssignmentService(INodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public Queue<UUID> getRandomNodeAssignments(int length) {
        List<UUID> originalIds = nodeRepository.getIdsOnline();

        if (originalIds.isEmpty()) {
            throw new NoAvailableNodesException("No hay nodos disponibles para almacenar el archivo");
        }

        // Mezclar todos los nodos disponibles de forma aleatoria
        Collections.shuffle(originalIds);

        List<UUID> nodeIds = new ArrayList<>();
        int originalSize = originalIds.size();

        // Se llena la lista combinando los nodos disponibles en orden cíclico
        for (int i = 0; i < length; i++) {
            nodeIds.add(originalIds.get(i % originalSize));
        }

        // Retornar directamente como una Queue (ArrayDeque)
        return new ArrayDeque<>(nodeIds);
    }

    public List<UUID> getDifferentsNodesIds(UUID nodeId) {
        List<UUID> nodeIds = nodeRepository.getIdsOnline();

        if (nodeIds.size() < REPLICATION_FACTOR - 1)
            throw new InsufficientNodesForReplicationException("No hay suficientes nodos para cumplir con el factor de replicacion. \n Nodos activos: " + nodeIds.size() + "\n Factor de replicacion: " + REPLICATION_FACTOR);

        Collections.shuffle(nodeIds);

        return nodeIds.stream().filter((id) -> !nodeId.equals(id)).limit(REPLICATION_FACTOR).toList();
    }
}

