package com.sdan.middleware.Infraestructure.out.Entities;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "file")
public class FileEntity {

    @Id
    @Column(name = "file_id")
    private UUID fileId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "folder_id", nullable = false)
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_type_id", nullable = false)
    private FileTypeEntity fileType;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "size", nullable = false)
    private Long size;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_modified", nullable = false)
    private Date dateModified;

    @Column(name = "total_partitions", nullable = false)
    private Integer totalPartitions;

    public FileEntity() {
    }

    public FileEntity(UUID fileId, FolderEntity folder, FileTypeEntity fileType, String name, Long size, Date dateCreated, Date dateModified, Integer totalPartitions) {
        this.fileId = fileId;
        this.folder = folder;
        this.fileType = fileType;
        this.name = name;
        this.size = size;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.totalPartitions = totalPartitions;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public FolderEntity getFolder() {
        return folder;
    }

    public void setFolder(FolderEntity folder) {
        this.folder = folder;
    }

    public FileTypeEntity getFileType() {
        return fileType;
    }

    public void setFileType(FileTypeEntity fileType) {
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public Integer getTotalPartitions() {
        return totalPartitions;
    }

    public void setTotalPartitions(Integer totalPartitions) {
        this.totalPartitions = totalPartitions;
    }
}

