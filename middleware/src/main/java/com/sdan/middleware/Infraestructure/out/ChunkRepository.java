package com.sdan.middleware.Infraestructure.out;

import com.sdan.middleware.Application.Ports.Repositories.IChunkRepository;
import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Infraestructure.out.Entities.ChunkEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FileEntity;
import com.sdan.middleware.Infraestructure.out.Entities.NodeEntity;
import com.sdan.middleware.Infraestructure.out.Mappers.ChunkMapper;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataChunkRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataFileRepository;
import com.sdan.middleware.Infraestructure.out.SpringData.SpringDataNodeRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChunkRepository implements IChunkRepository {

    private final SpringDataChunkRepository springDataChunkRepository;
    private final SpringDataFileRepository springDataFileRepository;
    private final SpringDataNodeRepository springDataNodeRepository;
    private final ChunkMapper chunkMapper;

    public ChunkRepository(
            SpringDataChunkRepository springDataChunkRepository,
            SpringDataFileRepository springDataFileRepository,
            SpringDataNodeRepository springDataNodeRepository,
            ChunkMapper chunkMapper) {
        this.springDataChunkRepository = springDataChunkRepository;
        this.springDataFileRepository = springDataFileRepository;
        this.springDataNodeRepository = springDataNodeRepository;
        this.chunkMapper = chunkMapper;
    }

    @Override
    public Optional<Chunk> getById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataChunkRepository.findById(id).map(chunkMapper::toDomain);
    }

    @Override
    public List<Chunk> getAll() {
        return springDataChunkRepository.findAll().stream()
                .map(chunkMapper::toDomain)
                .toList();
    }

    @Override
    public FileHash getFileHash(UUID id) {
        if (id == null) {
            return null;
        }
        return springDataChunkRepository.findFileHashByChunkId(id)
                .map(FileHash::new)
                .orElse(null);
    }

    @Override
    public List<Chunk> getFileChunks(UUID id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return springDataChunkRepository.findOriginalChunksByFileId(id).stream()
                .map(chunkMapper::toDomain)
                .toList();
    }

    @Override
    public List<Chunk> getReplicaNodes(UUID id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return springDataChunkRepository.findReplicaChunksByChunkId(id).stream()
                .map(chunkMapper::toDomain)
                .toList();
    }

    @Override
    public void save(Chunk chunk) {
        if (chunk == null) {
            return;
        }

        FileEntity fileEntity = null;
        if (chunk.getFile() != null && chunk.getFile().getId() != null) {
            fileEntity = springDataFileRepository.findById(chunk.getFile().getId()).orElse(null);
        }

        NodeEntity nodeEntity = null;
        if (chunk.getNode() != null && chunk.getNode().getId() != null) {
            nodeEntity = springDataNodeRepository.findById(chunk.getNode().getId()).orElse(null);
        }

        ChunkEntity entity = chunkMapper.toEntity(chunk, fileEntity, nodeEntity);
        springDataChunkRepository.save(entity);
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            return;
        }
        springDataChunkRepository.deleteById(id);
    }
}

