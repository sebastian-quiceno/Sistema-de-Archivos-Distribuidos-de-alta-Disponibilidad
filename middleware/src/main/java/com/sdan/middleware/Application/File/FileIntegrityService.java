package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Security.IFileHasher;
import com.sdan.middleware.Application.Ports.Storage.IFileIntegrityService;
import com.sdan.middleware.Application.Ports.Storage.IStorageService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.BuildingBlocks.Application.FileAuditStatus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileIntegrityService implements IFileIntegrityService {

    private final IChunkRepository chunkRepository;
    private final IStorageService storageService;
    private final IFileHasher fileHasher;
    private final ChunkReplicationService chunkReplicationService;

    public FileIntegrityService(
            IChunkRepository chunkRepository,
            IStorageService storageService,
            IFileHasher fileHasher,
            ChunkReplicationService chunkReplicationService) {
        this.chunkRepository = chunkRepository;
        this.storageService = storageService;
        this.fileHasher = fileHasher;
        this.chunkReplicationService = chunkReplicationService;
    }

    @Override
    public FileAuditStatus verifyAndHealFile(File file) {
        List<Chunk> chunks = chunkRepository.getFileChunks(file.getId());
        if (chunks == null || chunks.isEmpty()) {
            return FileAuditStatus.HEALTHY;
        }

        boolean fileHadCorruption = false;

        for (Chunk chunk : chunks) {
            boolean primaryHealthy = isChunkHealthy(chunk);

            List<Chunk> replicas = chunkRepository.getReplicaNodes(chunk.getId());
            if (replicas == null) {
                replicas = Collections.emptyList();
            }

            List<Chunk> corruptedChunks = new ArrayList<>();
            Chunk healthyChunk = null;

            if (primaryHealthy) {
                healthyChunk = chunk;
            } else {
                // El chunk original dañado debe incluirse entre los fragmentos a curar
                corruptedChunks.add(chunk);
                fileHadCorruption = true;
            }

            for (Chunk replica : replicas) {
                if (isChunkHealthy(replica)) {
                    if (healthyChunk == null) {
                        healthyChunk = replica;
                    }
                } else {
                    corruptedChunks.add(replica);
                    fileHadCorruption = true;
                }
            }

            if (healthyChunk == null) {
                // No se pudo recuperar ningún fragmento sano ni entre los originales ni en las réplicas
                return FileAuditStatus.UNRECOVERABLE;
            }

            // Curar todos los fragmentos dañados usando el fragmento íntegro
            for (Chunk corrupted : corruptedChunks) {
                chunkReplicationService.healChunk(corrupted, healthyChunk);
            }
        }

        return fileHadCorruption ? FileAuditStatus.HEALED : FileAuditStatus.HEALTHY;
    }

    private boolean isChunkHealthy(Chunk chunk) {
        try (InputStream stream = storageService.getObject(
                chunk.getNode().getName().getValue(),
                chunk.getNode().getRegionName().getValue(),
                chunk.getKey().getValue())) {

            if (stream == null) {
                return false;
            }

            return Boolean.TRUE.equals(fileHasher.Verify(stream, chunk.getFileHash().getValue()));
        } catch (Exception e) {
            return false;
        }
    }
}

