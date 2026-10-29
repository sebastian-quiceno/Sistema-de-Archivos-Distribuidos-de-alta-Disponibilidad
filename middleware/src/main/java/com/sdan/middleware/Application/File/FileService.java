package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Repositories.INodeRepository;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.Exceptions.Node.NodeNotFoundException;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.io.FileInputStream;
import java.util.UUID;

public class FileService {
    private static final Long MAX_SIZE_PER_CHUNK = 32L;


    private final IFileRepository fileRepository;
    private final IChunkRepository chunkRepository;
    private final INodeRepository nodeRepository;
    private final IStorageService storageService;
    private final IFileHasher fileHasher;

    public FileService(IFileRepository fileRepository, IChunkRepository chunkRepository, INodeRepository nodeRepository, IStorageService storageService, IFileHasher fileHasher) {
        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.nodeRepository = nodeRepository;
        this.storageService = storageService;
        this.fileHasher = fileHasher;
    }

    public void partitionFile(CreateFileCommand dto){
        long totalChunks = Math.ceilDiv(dto.size(), MAX_SIZE_PER_CHUNK);



    }

    //Funcion para subir un chunk a un nodo y guardar la metadata en la DB
    private void uploadToNode(UUID nodeId, File file, FileInputStream fileChunk, Boolean isReplica){
        Node node = nodeRepository.getById(nodeId).orElseThrow(
                ()-> new NodeNotFoundException("No se encontro el nodo con el id: '"+nodeId+"'")
        );

        String key = UUID.randomUUID() + file.getName().getValue();

        storageService.uploadObject(key, fileChunk, file.getBytesSize(), file.getFileType().getName().getValue());

        Chunk newChunk = Chunk.create(file, node, isReplica, file.getBytesSize(), getHashFromFile(fileChunk), new FileKey(key), new Version("zz"));

        chunkRepository.save(newChunk);

    }

    private FileHash getHashFromFile(FileInputStream file){
        return new FileHash(fileHasher.Hash(file));
    }

    private Long calculateTotalPartitions(Long size){
        return Math.ceilDiv(size, MAX_SIZE_PER_CHUNK);
    }
}
