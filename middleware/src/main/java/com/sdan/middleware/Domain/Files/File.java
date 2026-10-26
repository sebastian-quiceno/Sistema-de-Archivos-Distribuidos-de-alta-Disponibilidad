package com.sdan.middleware.Domain.Files;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;
import com.sdan.middleware.Domain.FileTypes.FileType;
import com.sdan.middleware.Domain.Files.ValueObjects.FileName;
import com.sdan.middleware.Domain.Folders.Folder;
import tools.jackson.databind.deser.jdk.UUIDDeserializer;

import java.util.Date;
import java.util.UUID;

public class File {
    private UUID id;
    private Folder folder;
    private FileType fileType;
    private FileName name;
    private Long size;
    private Integer totalPartitions;
    private Date dateCreated;
    private Date lastTimeModified;

    private File(UUID id, Folder folder, FileType fileType, FileName name, Long size, Integer totalPartitions, Date dateCreated, Date lastTimeModified) {
        this.id = id;
        this.folder = folder;
        this.fileType = fileType;
        this.name = name;
        this.size = Guard.positive(size, "file_size");
        this.totalPartitions = Guard.positive(totalPartitions, "total_partitions");
        this.dateCreated = dateCreated;
        this.lastTimeModified = lastTimeModified;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public Folder getFolder() {
        return folder;
    }

    public FileType getFileType() {
        return fileType;
    }

    public FileName getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public Integer getTotalPartitions() {
        return totalPartitions;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getLastTimeModified() {
        return lastTimeModified;
    }

    //Metodos constructores estaticos
    public static File create(Folder folder, FileType fileType, FileName name, Long size, Integer totalPartitions){
        return new File(UUID.randomUUID(), folder, fileType, name, size, totalPartitions, new Date(), new Date());
    }
    public static File reCreate(UUID id, Folder folder, FileType fileType, FileName name, Long size, Integer totalPartitions, Date dateCreated){
        return new File(id, folder, fileType, name, size, totalPartitions, dateCreated, new Date());
    }

    //metodos set
    public void changeFolder(Folder newFolder){
        folder = newFolder;
    }

    public void changeName(FileName newName){
        name = newName;
    }

    public void changeSize(Long newSize){
        size = newSize;
    }

    public void changeTotalPartitions(Integer newTotalPartitions){
        totalPartitions = newTotalPartitions;
    }

}
