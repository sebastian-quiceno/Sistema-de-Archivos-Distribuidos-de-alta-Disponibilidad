package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Inputs.Commands.CreateFileCommand;
import com.sdan.middleware.Application.Inputs.Messages.PurgeChunkMessage;
import com.sdan.middleware.Application.Inputs.Messages.ReplicateChunkMessage;
import com.sdan.middleware.Application.Ports.Messaging.IMessageQueuePort;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.FileType;
import com.sdan.middleware.Domain.Entities.Folder;
import com.sdan.middleware.Domain.ValueObjects.FileName;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FileService {
    public static final int REPLICATION_FACTOR = 3;

    private final IFileRepository fileRepository;
    private final IChunkRepository chunkRepository;
    private final FilePartitionService filePartitionService;
    private final NodeAssignmentService nodeAssignmentService;
    private final ChunkStorageService chunkStorageService;
    private final ChunkReplicationService chunkReplicationService;
    private final IMessageQueuePort messageQueuePort;

    public FileService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            FilePartitionService filePartitionService,
            NodeAssignmentService nodeAssignmentService,
            ChunkStorageService chunkStorageService,
            ChunkReplicationService chunkReplicationService) {
        this(
                fileRepository,
                chunkRepository,
                filePartitionService,
                nodeAssignmentService,
                chunkStorageService,
                chunkReplicationService,
                null
        );
    }

    public FileService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            FilePartitionService filePartitionService,
            NodeAssignmentService nodeAssignmentService,
            ChunkStorageService chunkStorageService,
            ChunkReplicationService chunkReplicationService,
            IMessageQueuePort messageQueuePort) {

        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.filePartitionService = filePartitionService;
        this.nodeAssignmentService = nodeAssignmentService;
        this.chunkStorageService = chunkStorageService;
        this.chunkReplicationService = chunkReplicationService;
        this.messageQueuePort = messageQueuePort;
    }

    public void uploadFile(FileName fileName, FileType fileType, Folder folder, InputStream file, long size) {

        // validateCommand(command);

        int totalChunks = filePartitionService.calculateTotalPartitions(size);

        Queue<UUID> nodeIds = nodeAssignmentService.getRandomNodeAssignments(totalChunks);

        File newFile = File.create(folder, fileType, fileName, size, Math.toIntExact(totalChunks));

        List<Chunk> chunks = new ArrayList<>();
        AtomicInteger partNumberCounter = new AtomicInteger(1);

        try {
            filePartitionService.partition(file, size, chunkBytes -> {
                UUID nodeId = nodeIds.poll();
                int partNumber = partNumberCounter.getAndIncrement();

                Chunk chunk = chunkStorageService.uploadToNode(nodeId, newFile, chunkBytes, false, partNumber);

                chunks.add(chunk);

                if (messageQueuePort == null) {
                    List<Chunk> replicas = chunkReplicationService.replicateFile(chunk, REPLICATION_FACTOR);
                    if (replicas != null) {
                        chunks.addAll(replicas);
                    }
                }
            });
        } catch (RuntimeException e) {
            // Compensación SAGA: limpiar chunks ya subidos si un fragmento intermedio falla
            for (Chunk uploadedChunk : chunks) {
                if (messageQueuePort != null && uploadedChunk.getNode() != null && uploadedChunk.getKey() != null) {
                    messageQueuePort.publishPurgeTask(new PurgeChunkMessage(
                            uploadedChunk.getNode().getName().getValue(),
                            uploadedChunk.getNode().getRegionName().getValue(),
                            uploadedChunk.getKey().getValue()
                    ));
                } else {
                    try {
                        chunkStorageService.deleteChunk(uploadedChunk);
                    } catch (Exception ignored) {
                    }
                }
            }
            throw e;
        }

        // Persistir la metadata después de procesar todos los fragmentos.
        fileRepository.save(newFile);

        for (Chunk chunk : chunks) {
            chunkRepository.save(chunk);
        }

        // Encolar replicación asíncrona mediante MOM (RabbitMQ)
        if (messageQueuePort != null) {
            for (Chunk chunk : chunks) {
                messageQueuePort.publishReplicationTask(
                        new ReplicateChunkMessage(chunk.getId(), REPLICATION_FACTOR)
                );
            }
        }
    }

    //TODO: revisar si es mejor sacarlo de la clase, e implementarlo en otra
    public void validateCommand(CreateFileCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser null");
        }

        if (command.size() < 0) {
            throw new IllegalArgumentException("El tamaño del archivo no puede ser negativo");
        }

        if (command.file() == null) {
            throw new IllegalArgumentException("El archivo no puede ser null");
        }
    }
}
