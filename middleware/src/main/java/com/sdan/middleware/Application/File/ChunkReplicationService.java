package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.util.List;
import java.util.UUID;

public class ChunkReplicationService {
    public static final int REPLICATION_FACTOR = 3;

    private final INodeRepository nodeRepository;
    private final IStorageService storageService;
    private final NodeAssignmentService nodeAssignmentService;

    public ChunkReplicationService(
            INodeRepository nodeRepository,
            IStorageService storageService,
            NodeAssignmentService nodeAssignmentService) {
        this.nodeRepository = nodeRepository;
        this.storageService = storageService;
        this.nodeAssignmentService = nodeAssignmentService;
    }

    //TODO: Revisar si hace falta pedir el Nodo al que se quiere replicar (Es mejor que lo haga el mismo)
    public void replicateFile(Chunk chunk, int replicationFactor) {

        Node arrivalNode;

        CopyToAnotherNodeCommand command;

        Node originalNode = nodeRepository.getById(chunk.getNode().getId()).orElseThrow(
                () -> new NodeNotFoundException("No se encontro el nodo con el ID: '" + chunk.getNode().getId() + "'")
        );

        List<UUID> arrivalNodesIds = nodeAssignmentService.getDifferentsNodesIds(chunk.getNode().getId(), replicationFactor);

        for (UUID arrivalNodeId : arrivalNodesIds) {

            arrivalNode = nodeRepository.getById(arrivalNodeId)
                    .orElseThrow(() -> new NodeNotFoundException(
                            "No se encontro el nodo con el ID: '" + arrivalNodeId + "'"
                    ));

            command = new CopyToAnotherNodeCommand(
                    originalNode.getRegionName().getValue(),
                    originalNode.getName().getValue(),
                    chunk.getKey().getValue(),
                    arrivalNode.getRegionName().getValue(),
                    arrivalNode.getName().getValue()
            );

            storageService.copyToAnotherNode(command);

            Chunk.create(
                    chunk.getFile(),
                    arrivalNode,
                    true,
                    chunk.getBytesSize(),
                    chunk.getFileHash(),
                    chunk.getKey(),
                    new Version("zz2"));
        }
    }

    //Revisar si elimar metodo... redundante (posible fachada)
    public void replicateChunk(UUID originalNodeId, Chunk chunk, int replica) {
        replicateFile(originalNodeId, chunk, replica);
    }
}

