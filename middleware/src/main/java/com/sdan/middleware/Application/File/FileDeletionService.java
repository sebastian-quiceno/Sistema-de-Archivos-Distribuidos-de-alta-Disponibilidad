package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Messages.PurgeChunkMessage;
import com.sdan.middleware.Application.Ports.Messaging.IMessageQueuePort;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Storage.IFileDeletionService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;

import java.util.ArrayList;
import java.util.List;

public class FileDeletionService implements IFileDeletionService {

    private final IFileRepository fileRepository;
    private final IChunkRepository chunkRepository;
    private final ChunkStorageService chunkStorageService;
    private final IMessageQueuePort messageQueuePort;

    public FileDeletionService(IFileRepository fileRepository, IChunkRepository chunkRepository, ChunkStorageService chunkStorageService) {
        this(fileRepository, chunkRepository, chunkStorageService, null);
    }

    public FileDeletionService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            ChunkStorageService chunkStorageService,
            IMessageQueuePort messageQueuePort) {
        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.chunkStorageService = chunkStorageService;
        this.messageQueuePort = messageQueuePort;
    }

    @Override
    public void deleteFile(File file) {
        if (file == null || file.getId() == null) {
            return;
        }

        // Se va a recopilar todos los chunks originales y replicas antes de eliminar la metadata
        List<Chunk> originalChunks = chunkRepository.getFileChunks(file.getId());
        List<Chunk> allChunksToDelete = new ArrayList<>();
        List<Chunk> replicas;

        if (originalChunks != null) {
            for (Chunk original : originalChunks) {
                allChunksToDelete.add(original);
                replicas = chunkRepository.getReplicaNodes(original.getId());
                if (replicas != null && !replicas.isEmpty()) {
                    allChunksToDelete.addAll(replicas);
                }
            }
        }

        // Se va a priorizar la consistencia de datos eliminando primero la metadata
        for (Chunk chunk : allChunksToDelete) {
            chunkRepository.deleteById(chunk.getId());
        }

        fileRepository.deleteById(file.getId());

        // Se encola la purga física en MOM (RabbitMQ) o se ejecuta directamente si MOM no está activo
        for (Chunk chunk : allChunksToDelete) {
            try {
                if (messageQueuePort != null && chunk.getNode() != null && chunk.getKey() != null) {
                    messageQueuePort.publishPurgeTask(new PurgeChunkMessage(
                            chunk.getNode().getName().getValue(),
                            chunk.getNode().getRegionName().getValue(),
                            chunk.getKey().getValue()
                    ));
                } else {
                    chunkStorageService.deleteChunk(chunk);
                }
            } catch (Exception ignored) {
                // Se captura el fallo individual para no interrumpir la limpieza de los demás nodos
            }
        }
    }
}