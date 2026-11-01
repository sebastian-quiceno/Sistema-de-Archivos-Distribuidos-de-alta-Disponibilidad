package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Domain.Exceptions.Node.InsufficientNodesForReplicationException;
import com.sdan.middleware.Domain.Exceptions.Node.NoAvailableNodesException;

import java.util.*;

public class NodeAssignmentService {

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

    public List<UUID> getDifferentsNodesIds(UUID nodeId, int replicationFactor) {
        List<UUID> nodeIds = nodeRepository.getIdsOnline();

        if (nodeIds.size() < replicationFactor - 1)
            throw new InsufficientNodesForReplicationException("No hay suficientes nodos para cumplir con el factor de replicacion. \n Nodos activos: " + nodeIds.size() + "\n Factor de replicacion: " + replicationFactor);

        Collections.shuffle(nodeIds);

        return nodeIds.stream().filter((id) -> !nodeId.equals(id)).limit(replicationFactor).toList();
    }
}

