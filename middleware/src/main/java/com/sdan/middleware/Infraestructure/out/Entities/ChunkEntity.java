package com.sdan.middleware.Infraestructure.out.Entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "chunk")
public class ChunkEntity {

    @Id
    @Column(name = "chunk_id")
    private UUID chunkId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id", nullable = false)
    private NodeEntity node;

    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Column(name = "is_replica", nullable = false)
    private Boolean isReplica;

    @Column(name = "bytes_size", nullable = false)
    private Long bytesSize;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "file_key", nullable = false, length = 116)
    private String fileKey;

    @Column(name = "version", length = 10)
    private String version;

    public ChunkEntity() {
    }

    public ChunkEntity(UUID chunkId, FileEntity file, NodeEntity node, Integer partNumber, Boolean isReplica, Long bytesSize, String fileHash, String fileKey, String version) {
        this.chunkId = chunkId;
        this.file = file;
        this.node = node;
        this.partNumber = partNumber;
        this.isReplica = isReplica;
        this.bytesSize = bytesSize;
        this.fileHash = fileHash;
        this.fileKey = fileKey;
        this.version = version;
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
    }

    public NodeEntity getNode() {
        return node;
    }

    public void setNode(NodeEntity node) {
        this.node = node;
    }

    public Integer getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(Integer partNumber) {
        this.partNumber = partNumber;
    }

    public Boolean getIsReplica() {
        return isReplica;
    }

    public void setIsReplica(Boolean isReplica) {
        this.isReplica = isReplica;
    }

    public Long getBytesSize() {
        return bytesSize;
    }

    public void setBytesSize(Long bytesSize) {
        this.bytesSize = bytesSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

