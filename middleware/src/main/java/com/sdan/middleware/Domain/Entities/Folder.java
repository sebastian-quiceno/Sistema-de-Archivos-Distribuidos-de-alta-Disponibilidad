package com.sdan.middleware.Domain.Entities;

import com.sdan.middleware.Domain.ValueObjects.FolderName;

import java.util.*;

public class Folder {
    private UUID folder_id;
    private User user;
    private List<Folder> folders;
    private FolderName name;
    private Date creation_date;

    private Folder(UUID folder_id, User user, List<Folder> folders, FolderName name, Date creation_date) {
        this.folder_id = folder_id;
        this.user = user;
        this.folders = folders;
        this.name = name;
        this.creation_date = creation_date;
    }

    //Getters
    public UUID getFolder_id() {
        return folder_id;
    }

    public User getUser() {
        return user;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public FolderName getName() {
        return name;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    //Metodos Creacionales estaticos
    public static Folder create(User user, FolderName folderName){
        return new Folder(UUID.randomUUID(), user, new ArrayList<>(), folderName, new Date());
    }

    public static Folder create(UUID id, User user, List<Folder> folders, FolderName folderName, Date date_created){
        return new Folder(id, user, folders, folderName, date_created);
    }

    //Metodos set
    public void addFolders(Folder newFolder){
        folders.add(newFolder);
    }

    public void removeFolders(Folder folder){
        folders.remove(folder);
    }

    public void changeFolderName(FolderName newName){
        name = newName;
    }

}
