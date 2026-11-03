package com.sdan.middleware.Infraestructure.out.SpringData;

import com.sdan.middleware.Infraestructure.out.Entities.ChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataChunkRepository extends JpaRepository<ChunkEntity, UUID> {

    @Query("SELECT c FROM ChunkEntity c WHERE c.file.fileId = :fileId AND c.isReplica = false ORDER BY c.partNumber ASC")
    List<ChunkEntity> findOriginalChunksByFileId(@Param("fileId") UUID fileId);

    @Query("SELECT c FROM ChunkEntity c WHERE c.file.fileId = :fileId ORDER BY c.partNumber ASC")
    List<ChunkEntity> findAllChunksByFileId(@Param("fileId") UUID fileId);

    @Query("SELECT c.fileHash FROM ChunkEntity c WHERE c.chunkId = :chunkId")
    Optional<String> findFileHashByChunkId(@Param("chunkId") UUID chunkId);

    @Query("SELECT c FROM ChunkEntity c WHERE c.file.fileId = (SELECT orig.file.fileId FROM ChunkEntity orig WHERE orig.chunkId = :chunkId) AND c.isReplica = true AND (c.fileKey = (SELECT orig.fileKey FROM ChunkEntity orig WHERE orig.chunkId = :chunkId) OR c.partNumber = (SELECT orig.partNumber FROM ChunkEntity orig WHERE orig.chunkId = :chunkId))")
    List<ChunkEntity> findReplicaChunksByChunkId(@Param("chunkId") UUID chunkId);
}

