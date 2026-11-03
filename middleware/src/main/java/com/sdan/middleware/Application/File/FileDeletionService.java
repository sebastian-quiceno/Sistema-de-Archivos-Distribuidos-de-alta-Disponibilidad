package com.sdan.middleware.Application.File;

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

    public FileDeletionService(IFileRepository fileRepository, IChunkRepository chunkRepository, ChunkStorageService chunkStorageService) {
        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
        this.chunkStorageService = chunkStorageService;
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

        // Se va a intentar eliminar los chunks físicos de los nodos de almacenamiento
        for (Chunk chunk : allChunksToDelete) {
            try {
                chunkStorageService.deleteChunk(chunk);
            } catch (Exception ignored) {
                // Se captura el fallo individual para no interrumpir la limpieza de los demás nodos
            }
        }
    }
}