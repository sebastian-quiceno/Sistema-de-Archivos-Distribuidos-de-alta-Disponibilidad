package com.sdan.middleware.Infraestructure.out.Entities;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "folder")
public class FolderEntity {

    @Id
    @Column(name = "folder_id")
    private UUID folderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dad_folder_id")
    private FolderEntity fatherFolder;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date dateCreated;

    public FolderEntity() {
    }

    public FolderEntity(UUID folderId, UserEntity user, FolderEntity fatherFolder, String name, Date dateCreated) {
        this.folderId = folderId;
        this.user = user;
        this.fatherFolder = fatherFolder;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public FolderEntity getFatherFolder() {
        return fatherFolder;
    }

    public void setFatherFolder(FolderEntity fatherFolder) {
        this.fatherFolder = fatherFolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}

