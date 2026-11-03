package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.util.ArrayList;
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

    public List<Chunk> replicateFile(Chunk chunk) {
        return replicateFile(chunk, REPLICATION_FACTOR);
    }

    //TODO: Revisar si hace falta pedir el Nodo al que se quiere replicar (Es mejor que lo haga el mismo)
    public List<Chunk> replicateFile(Chunk chunk, int replicationFactor) {

        Node arrivalNode;

        CopyToAnotherNodeCommand command;

        Node originalNode = nodeRepository.getById(chunk.getNode().getId()).orElseThrow(
                () -> new NodeNotFoundException("No se encontro el nodo con el ID: '" + chunk.getNode().getId() + "'")
        );

        List<UUID> arrivalNodesIds = nodeAssignmentService.getDifferentsNodesIds(chunk.getNode().getId(), replicationFactor);
        List<Chunk> replicas = new ArrayList<>();
        Chunk replica;

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

            replica = Chunk.create(
                    chunk.getFile(),
                    arrivalNode,
                    true,
                    chunk.getBytesSize(),
                    chunk.getFileHash(),
                    chunk.getKey(),
                    new Version("zz2"),
                    chunk.getPartNumber() != null ? chunk.getPartNumber() : 1);

            replicas.add(replica);
        }

        return replicas;
    }

    public void healChunk(Chunk corruptedChunk, Chunk saneChunk){
        Node arrivalNode = nodeRepository.getById(corruptedChunk.getNode().getId()).orElseThrow(
                () -> new NodeNotFoundException("No se encontro el nodo con el id: " + corruptedChunk.getNode().getId())
        );

        Node exitNode = nodeRepository.getById(saneChunk.getNode().getId()).orElseThrow(
                () -> new NodeNotFoundException("No se encontro el nodo con el id: " + saneChunk.getNode().getId())
        );

        // 1. Eliminar primero el archivo corrupto del nodo destino para liberar la ubicación
        storageService.deleteFileByKey(
                arrivalNode.getName().getValue(),
                arrivalNode.getRegionName().getValue(),
                corruptedChunk.getKey().getValue()
        );

        // 2. Copiar el fragmento íntegro desde el nodo sano hacia el nodo destino
        CopyToAnotherNodeCommand command = new CopyToAnotherNodeCommand(
                exitNode.getRegionName().getValue(),
                exitNode.getName().getValue(),
                saneChunk.getKey().getValue(),
                arrivalNode.getRegionName().getValue(),
                arrivalNode.getName().getValue()
        );

        storageService.copyToAnotherNode(command);
    }
}
