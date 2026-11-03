package com.sdan.middleware.Infraestructure.out.Mappers;

import com.sdan.middleware.Domain.Entities.Chunk;
import com.sdan.middleware.Domain.Entities.File;
import com.sdan.middleware.Domain.Entities.Node;
import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.Version;
import com.sdan.middleware.Infraestructure.out.Entities.ChunkEntity;
import com.sdan.middleware.Infraestructure.out.Entities.FileEntity;
import com.sdan.middleware.Infraestructure.out.Entities.NodeEntity;
import org.springframework.stereotype.Component;

@Component
public class ChunkMapper {

    private final FileMapper fileMapper;
    private final NodeMapper nodeMapper;

    public ChunkMapper(FileMapper fileMapper, NodeMapper nodeMapper) {
        this.fileMapper = fileMapper;
        this.nodeMapper = nodeMapper;
    }

    public Chunk toDomain(ChunkEntity entity) {
        if (entity == null) {
            return null;
        }

        File file = fileMapper.toDomain(entity.getFile());
        Node node = nodeMapper.toDomain(entity.getNode());

        return Chunk.reCreate(
                entity.getChunkId(),
                file,
                node,
                entity.getIsReplica(),
                entity.getBytesSize(),
                new FileHash(entity.getFileHash()),
                new FileKey(entity.getFileKey()),
                null,
                new Version(entity.getVersion() != null ? entity.getVersion() : "zz"),
                entity.getPartNumber()
        );
    }

    public ChunkEntity toEntity(Chunk domain, FileEntity fileEntity, NodeEntity nodeEntity) {
        if (domain == null) {
            return null;
        }
        return new ChunkEntity(
                domain.getId(),
                fileEntity,
                nodeEntity,
                domain.getPartNumber() != null ? domain.getPartNumber() : 1,
                domain.getReplica(),
                domain.getBytesSize(),
                domain.getFileHash() != null ? domain.getFileHash().getValue() : null,
                domain.getKey() != null ? domain.getKey().getValue() : null,
                domain.getVersion() != null ? domain.getVersion().getValue() : "zz"
        );
    }
}

