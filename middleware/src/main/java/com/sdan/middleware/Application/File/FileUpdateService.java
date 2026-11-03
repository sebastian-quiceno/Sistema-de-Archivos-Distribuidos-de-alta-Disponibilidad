package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Storage.IFileUpdateService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FileUpdateService implements IFileUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(FileUpdateService.class);

    private final IFileRepository fileRepository;
    private final IChunkRepository chunkRepository;
    private final FilePartitionService filePartitionService;
    private final NodeAssignmentService nodeAssignmentService;
    private final ChunkStorageService chunkStorageService;
    private final ChunkReplicationService chunkReplicationService;

    public FileUpdateService(
            IFileRepository fileRepository,
            IChunkRepository chunkRepository,
            FilePartitionService filePartitionService,
            NodeAssignmentService nodeAssignmentService,
            ChunkStorageService chunkStorageService,
            ChunkReplicationService chunkReplicationService) {
        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.filePartitionService = filePartitionService;
        this.nodeAssignmentService = nodeAssignmentService;
        this.chunkStorageService = chunkStorageService;
        this.chunkReplicationService = chunkReplicationService;
    }

    @Override
    public void updateFile(File file, InputStream newFile, Long newSize) {
        if (file == null || file.getId() == null) {
            throw new IllegalArgumentException("El archivo no puede ser nulo");
        }
        if (newFile == null) {
            throw new IllegalArgumentException("El contenido del archivo no puede ser nulo");
        }
        if (newSize == null || newSize < 0) {
            throw new IllegalArgumentException("El tamaño del archivo no puede ser nulo ni negativo");
        }

        // 1. Recopilar chunks originales y réplicas de la versión anterior antes de modificar nada
        List<Chunk> oldOriginalChunks = chunkRepository.getFileChunks(file.getId());
        List<Chunk> oldAllChunks = new ArrayList<>();
        if (oldOriginalChunks != null) {
            for (Chunk oldOriginal : oldOriginalChunks) {
                oldAllChunks.add(oldOriginal);
                List<Chunk> replicas = chunkRepository.getReplicaNodes(oldOriginal.getId());
                if (replicas != null && !replicas.isEmpty()) {
                    oldAllChunks.addAll(replicas);
                }
            }
        }

        // 2 & 3. Particionar, almacenar nuevos chunks y sus réplicas en los nodos
        int totalPartitions = filePartitionService.calculateTotalPartitions(newSize);
        Queue<UUID> nodeIds = nodeAssignmentService.getRandomNodeAssignments(totalPartitions);

        List<Chunk> newOriginalChunks = new ArrayList<>();
        List<Chunk> newAllChunks = new ArrayList<>();
        AtomicInteger partNumberCounter = new AtomicInteger(1);

        try {
            filePartitionService.partition(newFile, newSize, chunkBytes -> {
                UUID nodeId = nodeIds.poll();
                int partNumber = partNumberCounter.getAndIncrement();

                Chunk chunk = chunkStorageService.uploadToNode(nodeId, file, chunkBytes, false, partNumber);
                newOriginalChunks.add(chunk);
                newAllChunks.add(chunk);

                List<Chunk> replicas = chunkReplicationService.replicateFile(chunk, ChunkReplicationService.REPLICATION_FACTOR);
                if (replicas != null && !replicas.isEmpty()) {
                    newAllChunks.addAll(replicas);
                }
            });

            // 4. Verificar que los nuevos chunks y sus réplicas se hayan almacenado correctamente
            for (Chunk chunk : newAllChunks) {
                ChunkGetMethodOutput output = chunkStorageService.getChunk(chunk);
                if (output == null || output.corrupted()) {
                    throw new FileProcessingException(
                            "Fallo de integridad o almacenamiento en el nuevo chunk: " + chunk.getId()
                    );
                }
            }
        } catch (Exception e) {
            // Si falla la subida, replicación o verificación de los nuevos chunks:
            // Conservar intacta la versión anterior y limpiar los nuevos chunks ya subidos
            cleanUpStorageChunks(newAllChunks);
            throw e;
        }

        // 5. Actualizar la metadata del archivo en la base de datos
        List<Chunk> savedNewChunksInDb = new ArrayList<>();
        try {
            file.changeBytesSize(newSize);
            file.changeTotalPartitions(totalPartitions);

            for (Chunk chunk : newAllChunks) {
                chunkRepository.save(chunk);
                savedNewChunksInDb.add(chunk);
            }

            fileRepository.save(file);
        } catch (Exception e) {
            // Si falla la actualización de la metadata:
            // Limpiar chunks huérfanos nuevos en la base de datos y en los nodos
            for (Chunk savedChunk : savedNewChunksInDb) {
                try {
                    chunkRepository.deleteById(savedChunk.getId());
                } catch (Exception ignored) {}
            }
            cleanUpStorageChunks(newAllChunks);
            throw e;
        }

        // 6. Confirmada la metadata, eliminar los chunks y réplicas de la versión anterior
        // Se prioriza la persistencia de la nueva versión; fallos en la limpieza no abortan la operación
        for (Chunk oldChunk : oldAllChunks) {
            try {
                chunkRepository.deleteById(oldChunk.getId());
            } catch (Exception ex) {
                logger.warn("No se pudo eliminar la metadata del chunk antiguo: {}", oldChunk.getId(), ex);
            }
        }

        for (Chunk oldChunk : oldAllChunks) {
            try {
                chunkStorageService.deleteChunk(oldChunk);
            } catch (Exception ex) {
                logger.warn("No se pudo eliminar el chunk físico antiguo de almacenamiento: {}", oldChunk.getId(), ex);
            }
        }
    }

    private void cleanUpStorageChunks(List<Chunk> chunks) {
        if (chunks == null) return;
        for (Chunk chunk : chunks) {
            try {
                chunkStorageService.deleteChunk(chunk);
            } catch (Exception ex) {
                logger.warn("Error al limpiar chunk huérfano de almacenamiento: {}", chunk.getId(), ex);
            }
        }
    }
}

