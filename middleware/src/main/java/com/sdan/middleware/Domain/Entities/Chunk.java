package com.sdan.middleware.Domain.Entities;

import com.sdan.middleware.Domain.ValueObjects.FileHash;
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
    private Path path;
    private Version version;

    public Chunk(UUID id, File file, Node node, Boolean isReplica, Long bytesSize, Path path, Version version) {
        this.id = id;
        this.file = file;
        this.node = node;
        this.isReplica = isReplica;
        this.bytesSize = bytesSize;
        this.path = path;
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

    public Path getPath() {
        return path;
    }

    public Version getVersion() {
        return version;
    }

    //Metodos constructores
    public static Chunk create(File file, Node node, Boolean isReplica, Long bytesSize, Path path, Version version){
        return new Chunk(UUID.randomUUID(), file, node, isReplica, bytesSize, path, version);
    }

    public static Chunk reCreate(UUID id, File file, Node node, Boolean isReplica, Long bytesSize, Path path, Version version){
        return new Chunk(id, file, node, isReplica, bytesSize, path, version);
    }



}
