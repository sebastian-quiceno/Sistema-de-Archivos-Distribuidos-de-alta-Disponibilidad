package com.sdan.middleware.Domain.FileTypes;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;
import com.sdan.middleware.Domain.FileTypes.ValueObjects.FileTypeName;

import java.util.UUID;

public class FileType {

    private static final int MAX_DESCRIPTION_CHARACTERS = 50;

    private UUID id;
    private FileTypeName name;
    private String description;

    private FileType(UUID id, FileTypeName name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    //Getters

    public UUID getId() {
        return id;
    }

    public FileTypeName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


    //Metodos constructores estaticos
    public static FileType create(FileTypeName fileTypeName, String description){
        return new FileType(UUID.randomUUID(), fileTypeName, Guard.validText(description, "fileType_description", MAX_DESCRIPTION_CHARACTERS));
    }

    public static FileType reCreate(UUID id, FileTypeName fileTypeName, String description){
        return new FileType(Guard.validUUID(id, "fileType_id"), fileTypeName, Guard.validText(description, "fileType_description", MAX_DESCRIPTION_CHARACTERS));
    }

    //Metodos set
    public void changeName(FileTypeName newMame){
        name = newMame;
    }

    public void changeDescription(String newDescription){
        Guard.validText(newDescription, "fileType_description", MAX_DESCRIPTION_CHARACTERS);
        description = newDescription;
    }
}
