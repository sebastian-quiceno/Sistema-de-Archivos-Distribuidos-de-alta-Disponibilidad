package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.Chunk.ChunkCorruptedException;
import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ChunkStorageService {

    private final INodeRepository nodeRepository;
    private final IStorageService storageService;
    private final IFileHasher fileHasher;

    public ChunkStorageService(
            INodeRepository nodeRepository,
            IStorageService storageService,
            IFileHasher fileHasher) {
        this.nodeRepository = nodeRepository;
        this.storageService = storageService;
        this.fileHasher = fileHasher;
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

}

