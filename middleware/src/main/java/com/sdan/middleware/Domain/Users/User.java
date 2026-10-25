package com.sdan.middleware.Domain.Users;

import com.sdan.middleware.BuildingBlocks.Domain.Guard;
import com.sdan.middleware.Domain.Users.ValueObjects.PasswordHash;
import com.sdan.middleware.Domain.Users.ValueObjects.UserName;

import java.util.Date;
import java.util.UUID;

public class User {

    private UUID id;
    private UserName username;
    private PasswordHash passwordHash;
    private Date date_created;

    private User(UUID id, UserName username, PasswordHash passwordHash, Date date_created) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.date_created = date_created;
    }

    //Getters
    public UUID getId() {
        return id;
    }

    public UserName getUsername() {
        return username;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public Date getDate_created() {
        return date_created;
    }

    // Metodos contructores estaticos
    public static User create(UserName username, PasswordHash passwordHash, Date date_created){
        return new User(UUID.randomUUID(), username, passwordHash, date_created);
    }

    public static User reCreate(UUID id, UserName username, PasswordHash passwordHash, Date date_created){

        return new User(Guard.validUUID(id, "user_id"), username, passwordHash, date_created);
    }

    // Metodos set
    public void changeUsername(UserName newUserName ){
        username = newUserName;
    }

    public void changePasswordHash(PasswordHash newPasswordHash){
        passwordHash = newPasswordHash;
    }

}
