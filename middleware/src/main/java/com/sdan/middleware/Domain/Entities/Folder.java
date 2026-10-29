package com.sdan.middleware.Domain.Entities;

import com.sdan.middleware.Domain.ValueObjects.FolderName;

import java.util.*;


//TODO: REVISAR si se puede tener un padre null
public class Folder {
    private UUID folderId;
    private User user;
    private Folder fatherFolder;
    private FolderName name;
    private Date creationDate;

    private Folder(UUID folder_id, User user, Folder fatherFolder, FolderName name, Date creation_date) {
        this.folderId = folder_id;
        this.user = user;
        this.fatherFolder = fatherFolder;
        this.name = name;
        this.creationDate = creation_date;
    }

    //Getters
    public UUID getFolderId() {
        return folderId;
    }

    public User getUser() {
        return user;
    }

    public Folder getFatherFolder() {
        return fatherFolder;
    }

    public FolderName getName() {
        return name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    //Metodos Creacionales estaticos
    public static Folder create(User user, Folder fatherFolder, FolderName folderName){
        return new Folder(UUID.randomUUID(), user, fatherFolder, folderName, new Date());
    }

    public static Folder reCreate(UUID id, User user, Folder fatherFolder, FolderName folderName, Date date_created){
        return new Folder(id, user, fatherFolder, folderName, date_created);
    }

    //Metodos set
    public void changeFatherFolder(Folder newFatherFolder){
        fatherFolder = newFatherFolder;
    }

    public void changeFolderName(FolderName newName){
        name = newName;
    }

}
