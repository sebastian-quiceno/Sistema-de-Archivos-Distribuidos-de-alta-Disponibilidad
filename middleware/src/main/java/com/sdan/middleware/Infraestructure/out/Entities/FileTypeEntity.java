package com.sdan.middleware.Infraestructure.out.Entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "file_type")
public class FileTypeEntity {

    @Id
    @Column(name = "file_type_id")
    private UUID fileTypeId;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "description", nullable = false, length = 50)
    private String description;

    public FileTypeEntity() {
    }

    public FileTypeEntity(UUID fileTypeId, String name, String description) {
        this.fileTypeId = fileTypeId;
        this.name = name;
        this.description = description;
    }

    public UUID getFileTypeId() {
        return fileTypeId;
    }

    public void setFileTypeId(UUID fileTypeId) {
        this.fileTypeId = fileTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

