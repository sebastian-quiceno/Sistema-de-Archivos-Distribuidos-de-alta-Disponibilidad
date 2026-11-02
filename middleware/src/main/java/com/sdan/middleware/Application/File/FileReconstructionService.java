package com.sdan.middleware.Application.File;

import com.sdan.middleware.Application.Outputs.ChunkGetMethodOutput;
import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Application.Ports.Repositories.IFileRepository;
import com.sdan.middleware.Application.Ports.Storage.IFileReconstructionService;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Exceptions.File.FileIsCorruptedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;

public class FileReconstructionService implements IFileReconstructionService {
    private final IChunkRepository chunkRepository;
    private final IFileRepository fileRepository;
    private final ChunkStorageService chunkStorageService;

    public FileReconstructionService(IChunkRepository chunkRepository, IFileRepository fileRepository, ChunkStorageService chunkStorageService) {
        this.chunkRepository = chunkRepository;
        this.fileRepository = fileRepository;
        this.chunkStorageService = chunkStorageService;
    }

    @Override
    public InputStream mergeFile(File file) {

        List<Chunk> chunks = fileRepository.getOriginalsChunks(file.getId());
        
        if (chunks == null || chunks.isEmpty()) {
            return InputStream.nullInputStream();
        }

        // Ordenar siempre de manera ascendente por su índice de partición
        chunks = new ArrayList<>(chunks);
        chunks.sort(Comparator.comparing(c -> c.getPartNumber() != null ? c.getPartNumber() : 1));

        List<Chunk> replicas;
        List<InputStream> fileParts = new ArrayList<>();

        try {
            for (Chunk chunk : chunks) {
                ChunkGetMethodOutput response = chunkStorageService.getChunk(chunk);

                // Si el chunk original está corrupto, buscamos una réplica sana.
                if (response.corrupted()) {
                    replicas = chunkRepository.getReplicaNodes(chunk.getId());
                    if (replicas == null) {
                        replicas = Collections.emptyList();
                    }

                    response = replicas.stream()
                            .map(chunkStorageService::getChunk)
                            .filter(replica -> !replica.corrupted())
                            .findFirst()
                            .orElseThrow(() ->
                                    new FileIsCorruptedException("No se pudo recuperar el chunk: " + chunk.getKey().getValue())
                            );
                }

                fileParts.add(response.chunkFile());
            }

            return new SequenceInputStream(Collections.enumeration(fileParts));

        } catch (RuntimeException e) {
            // Si ocurre un error antes de devolver el stream,
            // cerramos los streams que ya habíamos abierto.
            fileParts.forEach(stream -> {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException closeException) {
                        e.addSuppressed(closeException);
                    }
                }
            });

            throw e;
        }
    }
}
