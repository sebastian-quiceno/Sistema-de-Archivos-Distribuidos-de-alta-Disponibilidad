package com.sdan.middleware.Domain.Entities;

import com.sdan.middleware.Domain.ValueObjects.FileHash;
import com.sdan.middleware.Domain.ValueObjects.FileKey;
import com.sdan.middleware.Domain.ValueObjects.Path;
import com.sdan.middleware.Domain.ValueObjects.Version;

import java.util.UUID;

public class Chunk {
    private UUID id;
    private File file;
    private Node node;
    private Boolean isReplica;
    private Long bytesSize;
    private FileHash fileHash;
    private FileKey key;
    private Version version;

    private Chunk(UUID id, File file, Node node, Boolean isReplica, Long bytesSize, FileHash fileHash, FileKey key, Version version) {
        this.id = id;
        this.file = file;
        this.node = node;
        this.isReplica = isReplica;
        this.bytesSize = bytesSize;
        this.fileHash = fileHash;
        this.key = key;
        this.version = version;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public Node getNode() {
        return node;
    }

    public Boolean getReplica() {
        return isReplica;
    }

    public Long getBytesSize() {
        return bytesSize;
    }

    public FileHash getFileHash() {
        return fileHash;
    }

    public FileKey getKey() {
        return key;
    }

    public Version getVersion() {
        return version;
    }

    //Metodos constructores
    public static Chunk create(File file, Node node, Boolean isReplica, Long bytesSize, FileHash fileHash, FileKey key, Version version){
        return new Chunk(UUID.randomUUID(), file, node, isReplica, bytesSize, fileHash, key, version);
    }

    public static Chunk reCreate(UUID id, File file, Node node, Boolean isReplica, Long bytesSize, FileHash fileHash, FileKey key, Path path, Version version){
        return new Chunk(id, file, node, isReplica, bytesSize, fileHash, key, version);
    }



}
