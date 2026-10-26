package com.sdan.middleware.Domain.Entities;

import com.sdan.middleware.Domain.ValueObjects.Path;
import com.sdan.middleware.Domain.ValueObjects.Version;
import org.w3c.dom.Node;

import java.util.UUID;

public class Chunk {
    private UUID id;
    private File file;
    private Node node;
    private Boolean isReplica;
    private Long bytesSize;
    private Path path;
    private Version version;

    private Chunk(UUID id, Boolean isReplica, Long bytesSize, Path path, Version version) {
        this.id = id;
        this.isReplica = isReplica;
        this.bytesSize = bytesSize;
        this.path = path;
        this.version = version;
    }

    //Getters
    public UUID getId() {
        return id;
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
    public static Chunk create(File file, ){}
}
