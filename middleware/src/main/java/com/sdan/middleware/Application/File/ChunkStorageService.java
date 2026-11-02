package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CopyToAnotherNodeCommand;
import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.File.FileIsCorruptedException;
import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkStorageService {

    private final INodeRepository nodeRepository;
    private final IStorageService storageService;
    private final IChunkRepository chunkRepository;
    private final IFileHasher fileHasher;
    private final ChunkReplicationService chunkReplicationService;

    public ChunkStorageService(INodeRepository nodeRepository, IStorageService storageService, IChunkRepository chunkRepository, IFileHasher fileHasher, ChunkReplicationService chunkReplicationService) {
        this.nodeRepository = nodeRepository;
        this.storageService = storageService;
        this.chunkRepository = chunkRepository;
        this.fileHasher = fileHasher;
        this.chunkReplicationService = chunkReplicationService;
    }

    public Chunk uploadToNode(UUID nodeId, File file, byte[] chunkBytes, boolean isReplica) {

        Node node = nodeRepository.getById(nodeId).orElseThrow(
                () -> new NodeNotFoundException("No se encontró el nodo con id: " + nodeId)
        );

        // La longitud y el hash corresponden al fragmento real.
        long chunkSize = chunkBytes.length;

        FileHash hash = new FileHash(fileHasher.Hash(new ByteArrayInputStream(chunkBytes)));

        // La clave no depende del nombre original del archivo.
        String key = UUID.randomUUID().toString();

        try (InputStream uploadStream = new ByteArrayInputStream(chunkBytes)) {
            storageService.uploadObject(node.getName().getValue(), node.getRegionName().getValue(), key, uploadStream, chunkSize, file.getFileType().getName().getValue());

        } catch (IOException e) {
            throw new FileProcessingException("No fue posible subir el fragmento al nodo " + nodeId + "\n", e);
        }

        return Chunk.create(
                file,
                node,
                isReplica,
                chunkSize,
                hash,
                new FileKey(key),
                new Version("zz"));
    }

    //TODO: Hacer que la funcion haga algo cuando el chunk esta corrupto
    public ChunkGetMethodOutput getChunk(Chunk chunk){
        InputStream chunkFile = storageService.getObject(chunk.getNode().getName().getValue(), chunk.getNode().getRegionName().getValue(), chunk.getKey().getValue());

        if(!fileHasher.Verify(chunkFile, chunk.getFileHash().getValue()))
            return new ChunkGetMethodOutput(chunk.getId(), null, true);

        return new ChunkGetMethodOutput(chunk.getId(), chunkFile, false);
    }

    public void lookOverIntegrity(File file) {

        List<Chunk> chunks = chunkRepository.getFileChunks(file.getId());

        for (Chunk chunk : chunks) {

            ChunkGetMethodOutput response = getChunk(chunk);

            if (!response.corrupted()) {
                continue;
            }

            List<Chunk> replicas = chunkRepository.getReplicaNodes(chunk.getId());

            List<Chunk> corruptedChunks = new ArrayList<>();
            Chunk healthyChunk = null;

            for (Chunk replica : replicas) {

                ChunkGetMethodOutput replicaResponse = getChunk(replica);

                if (replicaResponse.corrupted()) {
                    corruptedChunks.add(replica);
                } else if (healthyChunk == null) {
                    healthyChunk = replica;
                }
            }

            if (healthyChunk == null) {
                throw new FileIsCorruptedException("No se pudo recuperar el chunk: "+ chunk.getKey().getValue()
                );
            }

            for (Chunk corrupted : corruptedChunks) {

                chunkReplicationService.healChunk(
                        corrupted,
                        healthyChunk
                );
            }
        }
    }

}

